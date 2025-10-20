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

public class InventoryController {

    @FXML private Button queryButton;
    @FXML private Button closeButton;
    @FXML private Button deleteInventoryButton;
    @FXML private Button deleteDishButton;

    @FXML private TableView<Item> tableView;
    @FXML private TableColumn<Item, String> itemColumn;
    @FXML private TableColumn<Item, Integer> currentQtyColumn;
    @FXML private TableColumn<Item, Integer> recommendedColumn;
    @FXML private TableColumn<Item, Integer> differenceColumn;
    @FXML private ComboBox<String> itemSelect;
    @FXML private TextField quantityField;

    @FXML private ComboBox<String> priceItemSelect;
    @FXML private TextField priceField;

    @FXML private ComboBox<String> renameItemSelect;
    @FXML private TextField newNameField;

    @FXML private TableView<Dish> dishTableView;
    @FXML private TableColumn<Dish, String> dishNameColumn;
    @FXML private TableColumn<Dish, Double> dishPriceColumn;

    @FXML private ComboBox<String> renameDishSelect;
    @FXML private TextField newDishNameField;

    @FXML private TextField addInventoryField;
    @FXML private Button addInventoryButton;

    @FXML private TextField addDishNameField;
    @FXML private TextField addDishPriceField;
    @FXML private Button addDishButton;

    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/CSCE315Database";

    @FXML
    private void handleRestockPage() throws IOException { switchScene("/FXML/ManagerHome.fxml"); }

    @FXML
    private void handleOrderTrendsPage() throws IOException { switchScene("/FXML/OrderTrends.fxml"); }

    @FXML
    private void handleEmployeeDataPage() throws IOException { switchScene("/FXML/ManagerEmployeeData.fxml"); }

    @FXML
    private void handleLogout() throws IOException { switchScene("/FXML/Login.fxml"); }

    private void switchScene(String fxmlPath) throws IOException {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

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
            //conn.close();
            
            //added by alex for add button functionality
            /*
            int lastDish = 0;
            String sql2 = "SELECT dish_id FROM dish ORDER BY dish_id DESC LIMIT 1";
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.setInt(1, lastDish);
            ps2.executeUpdate();
            ps2.close();
            */


            conn.close();//end
            

            showAlert("Dish added successfully!");
            addDishNameField.clear();
            addDishPriceField.clear();
            loadDishes();
            loadPriceItemsForComboBox();
            loadRenameDishesForComboBox();
            /*
            ServerDefaultController controller = new ServerDefaultController();
            controller.addNewButton(name, lastDish);//addNewButton to add item to serverDefault
            */
            

        } catch (Exception e) { e.printStackTrace(); showAlert("Error adding dish: " + e.getMessage()); }
    }

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

    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Database Update");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Item {
        private final StringProperty name;
        private final IntegerProperty current;
        private final IntegerProperty recommended;
        private final IntegerProperty difference;

        public Item(String name, int current, int recommended) {
            this.name = new SimpleStringProperty(name);
            this.current = new SimpleIntegerProperty(current);
            this.recommended = new SimpleIntegerProperty(recommended);
            this.difference = new SimpleIntegerProperty(recommended - current);
        }

        public StringProperty nameProperty() { return name; }
        public IntegerProperty currentProperty() { return current; }
        public IntegerProperty recommendedProperty() { return recommended; }
        public IntegerProperty differenceProperty() { return difference; }
        public String getName() { return name.get(); }
        public int getCurrent() { return current.get(); }
        public int getRecommended() { return recommended.get(); }
    }

    public static class Dish {
        private final StringProperty name;
        private final DoubleProperty price;

        public Dish(String name, double price) {
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
        }

        public StringProperty nameProperty() { return name; }
        public DoubleProperty priceProperty() { return price; }
    }
}
