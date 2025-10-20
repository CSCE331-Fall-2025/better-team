import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.beans.property.*;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
* Controller class for managing the inventory and dish data.
* This controller connects to the corresponding FXML layout and handles UI interactions such as
* adding, deleting, renaming, and updating items and dishes. It also performs database operations
* to reflect changes in PostgreSQL.
*/
public class InventoryController {

    /** Button that triggers loading or refreshing the inventory table data. */
    @FXML private Button queryButton;

    /** Button that closes the current window or scene. */
    @FXML private Button closeButton;
    
    /** Button that deletes a selected item from the inventory table. */
    @FXML private Button deleteInventoryButton;

    /** Button that deletes a selected dish from the dish table. */
    @FXML private Button deleteDishButton;

    /** TableView that displays inventory items. */
    @FXML private TableView<Item> tableView;

    /** Column displaying the name of the inventory item. */
    @FXML private TableColumn<Item, String> itemColumn;

    /** Column displaying the current quantity in stock for each item. */
    @FXML private TableColumn<Item, Integer> currentQtyColumn;

    /** Column displaying the recommended (target) quantity for each item. */
    @FXML private TableColumn<Item, Integer> recommendedColumn;

    /** Column displaying the difference between recommended and current quantities. */
    @FXML private TableColumn<Item, Integer> differenceColumn;

    /** ComboBox allowing the manager to select an inventory item to restock. */
    @FXML private ComboBox<String> itemSelect;

    /** Text field for entering the new quantity value for the selected inventory item. */
    @FXML private TextField quantityField;

    /** ComboBox used to select a dish whose price needs to be updated. */
    @FXML private ComboBox<String> priceItemSelect;

    /** Text field for entering a new price for the selected dish. */
    @FXML private TextField priceField;
    
    /** ComboBox allowing selection of an inventory item to rename. */
    @FXML private ComboBox<String> renameItemSelect;
    
    /** Text field for entering the new name for the selected inventory item. */
    @FXML private TextField newNameField;
    
    /** TableView that displays all dishes on the menu. Each row represents a {@link Dish}. */
    @FXML private TableView<Dish> dishTableView;
    
    /** Column displaying the name of the dish. */
    @FXML private TableColumn<Dish, String> dishNameColumn;
    
    /** Column displaying the price of the dish. */
    @FXML private TableColumn<Dish, Double> dishPriceColumn;
    
    /** ComboBox allowing selection of a dish to rename. */
    @FXML private ComboBox<String> renameDishSelect;
    
    /** Text field for entering the new name of the selected dish. */
    @FXML private TextField newDishNameField;
    
    /** Text field for entering the name of a new inventory item to add. */
    @FXML private TextField addInventoryField;
    
    /** Button that adds a new inventory item when clicked. */
    @FXML private Button addInventoryButton;
    
    /** Text field for entering the name of a new dish to add to the menu. */
    @FXML private TextField addDishNameField;
    
    /** Text field for entering the price of the new dish. */
    @FXML private TextField addDishPriceField;
    
    /** Button that adds a new dish to the database when clicked. */
    @FXML private Button addDishButton;

    /** Connects to the PostSQL database */
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/CSCE315Database";

    /** Handles navigation to the Restock page. */
    @FXML
    private void handleRestockPage() throws IOException { switchScene("/FXML/ManagerHome.fxml"); }

    /** Handles navigation to the OrderTrends page. */
    @FXML
    private void handleOrderTrendsPage() throws IOException { switchScene("/FXML/OrderTrends.fxml"); }

    /** Handles navigation to the Employee Data page. */
    @FXML
    private void handleEmployeeDataPage() throws IOException { switchScene("/FXML/ManagerEmployeeData.fxml"); }

    /** Logs the current user out and navigates back to the Login page. */
    @FXML
    private void handleLogout() throws IOException { switchScene("/FXML/Login.fxml"); }

    /** Switches the current JavaFX scene to a new one defined by the given FXML file path. */
    private void switchScene(String fxmlPath) throws IOException {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
    * Initializes the controller after its FXML components have been loaded.
    * This method configures table columns, sets up editable cells, defines event handlers
    * for buttons, and loads initial data for inventory and dishes into the UI.
    * It also populates all ComboBoxes used for updating, renaming, and adding inventory or dish items.
    */
    @FXML
    public void initialize() {
        tableView.setEditable(true);

        itemColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        currentQtyColumn.setCellValueFactory(cell -> cell.getValue().currentProperty().asObject());
        recommendedColumn.setCellValueFactory(cell -> cell.getValue().recommendedProperty().asObject());
        differenceColumn.setCellValueFactory(cell -> cell.getValue().differenceProperty().asObject());

        recommendedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        recommendedColumn.setOnEditCommit(event -> {
            Item item = event.getRowValue();
            int newTarget = event.getNewValue();
            item.recommendedProperty().set(newTarget);
            updateTargetQuantityInDB(item.getName(), newTarget);
            item.differenceProperty().set(item.getRecommended() - item.getCurrent());
            tableView.refresh();
        });

        dishNameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        dishPriceColumn.setCellValueFactory(cell -> cell.getValue().priceProperty().asObject());

        queryButton.setOnAction(e -> loadInventory());
        closeButton.setOnAction(e -> closeWindow());
        deleteInventoryButton.setOnAction(e -> handleDeleteInventory());
        deleteDishButton.setOnAction(e -> handleDeleteDish());

        addInventoryButton.setOnAction(e -> handleAddInventory());
        addDishButton.setOnAction(e -> handleAddDish());

        loadInventory();
        loadDishes();
        loadItemsForComboBox();
        loadPriceItemsForComboBox();
        loadRenameItemsForComboBox();
        loadRenameDishesForComboBox();
    }

    /**
    * Loads all inventory items from the database and displays them in the inventory table.
    * This method connects to the PostgreSQL database, retrieves item information including
    * the name, current quantity, and target (recommended) quantity, and populates the
    * {@link #tableView} with the results.
    * If an error occurs during the database query, an alert is shown to the user.
    */
    private void loadInventory() {
        List<Item> items = new ArrayList<>();
        try {
            dbSetup my = new dbSetup();
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            String sql = "SELECT item, current_inventory, target_inventory FROM inventory";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String name = rs.getString("item");
                int current = rs.getInt("current_inventory");
                int recommended = rs.getInt("target_inventory");
                items.add(new Item(name, current, recommended));
            }

            tableView.setItems(FXCollections.observableArrayList(items));

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading inventory: " + e.getMessage());
        }
    }

    /**
    * Loads all dish entries from the database and displays them in the dish table.
    * This method connects to the PostgreSQL database, retrieves each dish's name and price
    * from the {@code dish} table, and populates the {@link #dishTableView} with the data.
    * If an exception occurs during database access, an error alert is displayed to the user.
    */
    private void loadDishes() {
        List<Dish> dishes = new ArrayList<>();
        try {
            dbSetup my = new dbSetup();
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            String sql = "SELECT name, price FROM dish";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                dishes.add(new Dish(name, price));
            }

            dishTableView.setItems(FXCollections.observableArrayList(dishes));

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading dishes: " + e.getMessage());
        }
    }

    /**
    * Loads all inventory item names from the database into the item selection ComboBox.
    * This method queries the {@code inventory} table for item names and populates the
    * {@link #itemSelect} ComboBox with the retrieved values. It clears existing entries
    * before adding new ones to ensure the list is up to date.
    * Any database connection or query errors are printed to the console.
    */
    private void loadItemsForComboBox() {
        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT item FROM inventory");

            itemSelect.getItems().clear();
            while (rs.next()) { itemSelect.getItems().add(rs.getString("item")); }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
    * Loads all dish names from the database into the price update ComboBox.
    * This method queries the {@code dish} table for dish names and populates the
    * {@link #priceItemSelect} ComboBox. Existing items in the ComboBox are cleared
    * before adding new ones to ensure the list reflects current database entries.
    * Any database connection or query errors are printed to the console.
    */
    private void loadPriceItemsForComboBox() {
        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM dish");

            priceItemSelect.getItems().clear();
            while (rs.next()) { priceItemSelect.getItems().add(rs.getString("name")); }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
    * Loads all inventory item names from the database into the rename ComboBox.
    * This method retrieves item names from the {@code inventory} table and populates
    * the {@link #renameItemSelect} ComboBox. Existing items are cleared first to ensure
    * the ComboBox displays the most up to date list of inventory items.
    * Any exceptions during database connection or query execution are printed to the console.
    */
    private void loadRenameItemsForComboBox() {
        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT item FROM inventory");

            renameItemSelect.getItems().clear();
            while (rs.next()) { renameItemSelect.getItems().add(rs.getString("item")); }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
    * Loads all dish names from the database into the rename dish ComboBox.
    * This method retrieves dish names from the {@code dish} table and populates
    * the {@link #renameDishSelect} ComboBox. Existing entries are cleared first
    * to ensure the ComboBox displays the latest data from the database.
    * Any exceptions during database connection or query execution are printed to the console.
    */
    private void loadRenameDishesForComboBox() {
        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM dish");

            renameDishSelect.getItems().clear();
            while (rs.next()) { renameDishSelect.getItems().add(rs.getString("name")); }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
    * Handles restocking an inventory item based on user input from the ComboBox and TextField.
    * Checks that an item is selected and the quantity is a valid number.
    * Updates the corresponding record in the database and refreshes the inventory table.
    * Displays an alert to inform the user of success, failure, or input errors.
    */
    @FXML
    private void handleRestock() {
        String selectedItem = itemSelect.getValue();
        if (selectedItem == null || selectedItem.isEmpty()) {
            showAlert("Please select an item to restock.");
            return;
        }

        int quantity;
        try { quantity = Integer.parseInt(quantityField.getText()); }
        catch (NumberFormatException e) { showAlert("Please enter a valid number for quantity."); return; }

        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            String sql = "UPDATE inventory SET current_inventory = ? WHERE item = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, quantity);
            ps.setString(2, selectedItem);
            int updated = ps.executeUpdate();

            showAlert(updated > 0 ? "Inventory updated successfully!" : "No rows updated. Check the item name.");

            ps.close();
            conn.close();
            loadInventory();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error updating inventory: " + e.getMessage()); }
    }

    /**
    * Handles renaming an inventory item based on user input from the rename ComboBox and TextField.
    * Validates that an old item is selected and a new name is provided.
    * Updates the item name in the database and refreshes the inventory table and related ComboBoxes.
    * Displays alerts to inform the user of success, failure, or input errors.
    */
    @FXML
    private void handleRenameItem() {
        String oldName = renameItemSelect.getValue();
        String newName = newNameField.getText().trim();

        if (oldName == null || oldName.isEmpty()) { showAlert("Please select an item to rename."); return; }
        if (newName.isEmpty()) { showAlert("Please enter a new name."); return; }

        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            String sql = "UPDATE inventory SET item = ? WHERE item = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newName);
            ps.setString(2, oldName);
            int updated = ps.executeUpdate();

            showAlert(updated > 0 ? "Item renamed successfully!" : "No rows updated. Check the item name.");

            ps.close();
            conn.close();

            loadInventory();
            loadItemsForComboBox();
            loadRenameItemsForComboBox();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error renaming item: " + e.getMessage()); }
    }

    /**
    * Handles renaming a dish based on user input from the rename dish ComboBox and TextField.
    * Validates that a dish is selected and a new name is provided.
    * Updates the dish name in the database and refreshes the dish table and related ComboBoxes.
    * Displays alerts to inform the user of success, failure, or input errors.
    */
    @FXML
    private void handleRenameDish() {
        String oldName = renameDishSelect.getValue();
        String newName = newDishNameField.getText().trim();

        if (oldName == null || oldName.isEmpty()) { showAlert("Please select a dish to rename."); return; }
        if (newName.isEmpty()) { showAlert("Please enter a new dish name."); return; }

        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            String sql = "UPDATE dish SET name = ? WHERE name = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newName);
            ps.setString(2, oldName);
            int updated = ps.executeUpdate();

            showAlert(updated > 0 ? "Dish renamed successfully!" : "No rows updated. Check the dish name.");

            ps.close();
            conn.close();

            loadDishes();
            loadPriceItemsForComboBox();
            loadRenameDishesForComboBox();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error renaming dish: " + e.getMessage()); }
    }

    /**
    * Handles updating the price of a selected dish based on user input from the price ComboBox and TextField.
    * Validates that a dish is selected and the new price is a valid number.
    * Updates the dish's price in the database and refreshes the dish table and price ComboBox.
    * Displays alerts to inform the user of success, failure, or input errors.
    */
    @FXML
    private void handlePriceUpdate() {
        String selectedDish = priceItemSelect.getValue();
        if (selectedDish == null || selectedDish.isEmpty()) { showAlert("Please select a dish to update."); return; }

        double newPrice;
        try { newPrice = Double.parseDouble(priceField.getText()); }
        catch (NumberFormatException e) { showAlert("Please enter a valid price (e.g., 9.99)."); return; }

        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            String sql = "UPDATE dish SET price = ? WHERE name = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, newPrice);
            ps.setString(2, selectedDish);
            int updated = ps.executeUpdate();

            showAlert(updated > 0 ? "Dish price updated successfully!" : "No rows updated. Check the dish name.");

            ps.close();
            conn.close();
            loadDishes();
            loadPriceItemsForComboBox();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error updating price: " + e.getMessage()); }
    }

    /**
    * Handles adding a new inventory item based on user input from the add item TextField.
    * Validates that an item name is provided.
    * Inserts a new record into the inventory table with initial current and target inventory set to 0.
    * Refreshes the inventory table and relevant ComboBoxes, and clears the input field.
    * Displays alerts to inform the user of success or any errors.
    */
    @FXML
    private void handleAddInventory() {
        String itemName = addInventoryField.getText().trim();
        if (itemName.isEmpty()) { showAlert("Enter an item name to add."); return; }

        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            String sql = "INSERT INTO inventory(item, current_inventory, target_inventory) VALUES (?, 0, 0)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, itemName);
            ps.executeUpdate();

            ps.close();
            conn.close();

            showAlert("Item added successfully!");
            addInventoryField.clear();
            loadInventory();
            loadItemsForComboBox();
            loadRenameItemsForComboBox();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error adding inventory: " + e.getMessage()); }
    }

    /**
    * Handles adding a new dish based on user input from the add dish TextFields.
    * Validates that both a dish name and a price are provided and that the price is a valid number.
    * Inserts a new record into the dish table with type set to 'Other'.
    * Refreshes the dish table and related ComboBoxes, and clears the input fields.
    * Displays alerts to inform the user of success or any errors.
    */
    @FXML
    private void handleAddDish() {
        String name = addDishNameField.getText().trim();
        String priceText = addDishPriceField.getText().trim();

        if (name.isEmpty() || priceText.isEmpty()) { showAlert("Enter both dish name and price."); return; }

        double price;
        try { price = Double.parseDouble(priceText); }
        catch (NumberFormatException e) { showAlert("Enter a valid price."); return; }

        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            String sql = "INSERT INTO dish(name, price, type) VALUES (?, ?, 'Other')";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.executeUpdate();

            ps.close();
            conn.close();

            showAlert("Dish added successfully!");
            addDishNameField.clear();
            addDishPriceField.clear();
            loadDishes();
            loadPriceItemsForComboBox();
            loadRenameDishesForComboBox();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error adding dish: " + e.getMessage()); }
    }

    /**
    * Handles deleting the selected inventory item from the table and database.
    * Validates that an item is selected before attempting deletion.
    * Removes the item from the inventory table in the database and refreshes the table and relevant ComboBoxes.
    * Displays alerts to inform the user of success, failure, or errors.
    */
    @FXML
    private void handleDeleteInventory() {
        Item selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Please select an item to delete.");
            return;
        }

        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            String sql = "DELETE FROM inventory WHERE item = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, selectedItem.getName());
            int deleted = ps.executeUpdate();

            ps.close();
            conn.close();

            showAlert(deleted > 0 ? "Item deleted successfully!" : "No rows deleted.");
            loadInventory();
            loadItemsForComboBox();
            loadRenameItemsForComboBox();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error deleting item: " + e.getMessage());
        }
    }

    /**
     * Handles deleting the selected dish from the table and database.
     * Validates that a dish is selected before attempting deletion.
     * Removes the dish from the dish table in the database and refreshes the table and relevant ComboBoxes.
     * Displays alerts to inform the user of success, failure, or errors.
    */
    @FXML
    private void handleDeleteDish() {
        Dish selectedDish = dishTableView.getSelectionModel().getSelectedItem();
        if (selectedDish == null) {
            showAlert("Please select a dish to delete.");
            return;
        }

        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            String sql = "DELETE FROM dish WHERE name = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, selectedDish.nameProperty().get());
            int deleted = ps.executeUpdate();

            ps.close();
            conn.close();

            showAlert(deleted > 0 ? "Dish deleted successfully!" : "No rows deleted.");
            loadDishes();
            loadPriceItemsForComboBox();
            loadRenameDishesForComboBox();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error deleting dish: " + e.getMessage());
        }
    }

    /**
    * Updates the target inventory quantity for a specific item in the database.
    * @param itemName the name of the inventory item to update
    * @param newTarget the new target quantity to set for the item
    * Displays an alert if an error occurs during the database update.
    */
    private void updateTargetQuantityInDB(String itemName, int newTarget) {
        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            String sql = "UPDATE inventory SET target_inventory = ? WHERE item = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, newTarget);
            ps.setString(2, itemName);
            ps.executeUpdate();

            ps.close();
            conn.close();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error updating target quantity: " + e.getMessage()); }
    }

    /**
    * Closes the current window or stage.
    * Retrieves the stage from the close button's scene and closes it.
    */
    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
    * Displays an informational alert dialog with the given message.
    * @param message the message to show in the alert content
    */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Database Update");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
    * Represents an inventory item with properties for name, current quantity,
    * recommended (target) quantity, and the difference between recommended and current.
    * Designed for use with JavaFX TableView and property bindings.
    */
    public static class Item {
        private final StringProperty name;
        private final IntegerProperty current;
        private final IntegerProperty recommended;
        private final IntegerProperty difference;

        /**
        * Constructs an Item with the specified name, current quantity, and recommended quantity.
        * @param name the name of the item
        * @param current the current quantity of the item
        * @param recommended the recommended (target) quantity of the item
        */
        public Item(String name, int current, int recommended) {
            this.name = new SimpleStringProperty(name);
            this.current = new SimpleIntegerProperty(current);
            this.recommended = new SimpleIntegerProperty(recommended);
            this.difference = new SimpleIntegerProperty(recommended - current);
        }

        /** Returns the name property of the item. */
        public StringProperty nameProperty() { return name; }

        /** Returns the current quantity property of the item. */
        public IntegerProperty currentProperty() { return current; }

        /** Returns the recommended quantity property of the item. */
        public IntegerProperty recommendedProperty() { return recommended; }

        /** Returns the difference property (recommended - current) of the item. */
        public IntegerProperty differenceProperty() { return difference; }

        /** Returns the current name of the item. */
        public String getName() { return name.get(); }

        /** Returns the current quantity of the item. */
        public int getCurrent() { return current.get(); }

        /** Returns the recommended quantity of the item. */
        public int getRecommended() { return recommended.get(); }
    }

    /**
    * Represents a dish with a name and price.
    * Designed for use with JavaFX TableView and property bindings.
    */
    public static class Dish {
        private final StringProperty name;
        private final DoubleProperty price;

        /**
        * Constructs a Dish with the specified name and price.
        * @param name the name of the dish
        * @param price the price of the dish
        */
        public Dish(String name, double price) {
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
        }

        /** Returns the name property of the dish. */
        public StringProperty nameProperty() { return name; }

        /** Returns the price property of the dish. */
        public DoubleProperty priceProperty() { return price; }
    }
}
