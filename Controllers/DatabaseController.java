import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Database.Dish;
import Database.Item;

public class DatabaseController {

    @FXML private Button queryButton;
    @FXML private Button closeButton;

    @FXML private TableView<Item> tableView;
    @FXML private TableColumn<Item, String> itemColumn;
    @FXML private TableColumn<Item, Integer> currentQtyColumn;
    @FXML private TableColumn<Item, Integer> recommendedColumn;
    @FXML private TableColumn<Item, Integer> differenceColumn;
    @FXML private ComboBox<String> itemSelect;
    @FXML private TextField quantityField;
    @FXML private TextArea resultArea;

    @FXML private ComboBox<String> priceItemSelect;
    @FXML private TextField priceField;

    @FXML private TableView<Dish> dishTableView;
    @FXML private TableColumn<Dish, String> dishNameColumn;
    @FXML private TableColumn<Dish, Double> dishPriceColumn;

    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/CSCE315Database";

    @FXML
    public void initialize() {
        itemColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        currentQtyColumn.setCellValueFactory(cell -> cell.getValue().currentProperty().asObject());
        recommendedColumn.setCellValueFactory(cell -> cell.getValue().recommendedProperty().asObject());
        differenceColumn.setCellValueFactory(cell -> cell.getValue().differenceProperty().asObject());

        dishNameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        dishPriceColumn.setCellValueFactory(cell -> cell.getValue().priceProperty().asObject());

        queryButton.setOnAction(e -> loadInventory());
        closeButton.setOnAction(e -> closeWindow());

        loadInventory();
        loadDishes();
        loadItemsForComboBox();
        loadPriceItemsForComboBox();
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
            while (rs.next()) {
                itemSelect.getItems().add(rs.getString("item"));
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRestock() {
        String selectedItem = itemSelect.getValue();
        if (selectedItem == null || selectedItem.isEmpty()) {
            showAlert("Please select an item to restock.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid number for quantity.");
            return;
        }

        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            String sql = "UPDATE inventory SET current_inventory = ? WHERE item = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, quantity);
            ps.setString(2, selectedItem);
            int updated = ps.executeUpdate();

            if (updated > 0) {
                showAlert("Inventory updated successfully!");
            } else {
                showAlert("No rows updated. Check the item name.");
            }

            ps.close();
            conn.close();
            loadInventory();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error updating inventory: " + e.getMessage());
        }
    }

    private void loadPriceItemsForComboBox() {
        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM dish");

            priceItemSelect.getItems().clear();
            while (rs.next()) {
                priceItemSelect.getItems().add(rs.getString("name"));
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePriceUpdate() {
        String selectedDish = priceItemSelect.getValue();
        if (selectedDish == null || selectedDish.isEmpty()) {
            showAlert("Please select a dish to update.");
            return;
        }

        double newPrice;
        try {
            newPrice = Double.parseDouble(priceField.getText());
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid price (e.g., 9.99).");
            return;
        }

        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            String sql = "UPDATE dish SET price = ? WHERE name = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, newPrice);
            ps.setString(2, selectedDish);
            int updated = ps.executeUpdate();

            if (updated > 0) {
                showAlert("Dish price updated successfully!");
            } else {
                showAlert("No rows updated. Check the dish name.");
            }

            ps.close();
            conn.close();
            loadDishes();
            loadPriceItemsForComboBox();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error updating price: " + e.getMessage());
        }
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
}
