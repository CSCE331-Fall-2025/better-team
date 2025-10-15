package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.InventoryModel;
import model.Item;

import java.util.List;

public class InventoryController {

    @FXML private Button queryButton;
    @FXML private Button closeButton;
    @FXML private TableView<Item> tableView;
    @FXML private TableColumn<Item, String> itemColumn;
    @FXML private TableColumn<Item, Integer> currentQtyColumn;
    @FXML private TableColumn<Item, Integer> recommendedColumn;
    @FXML private TableColumn<Item, Integer> differenceColumn;
    @FXML private ComboBox<?> itemSelect;
    @FXML private TextField quantityField;
    @FXML private TextArea resultArea;

    private InventoryModel model;

    @FXML
    public void initialize() {
        model = new InventoryModel();

        itemColumn.setCellValueFactory(cell -> cell.getValue().itemNameProperty());
        currentQtyColumn.setCellValueFactory(cell -> cell.getValue().currentQtyProperty().asObject());
        recommendedColumn.setCellValueFactory(cell -> cell.getValue().recommendedProperty().asObject());
        differenceColumn.setCellValueFactory(cell -> cell.getValue().differenceProperty().asObject());

        queryButton.setOnAction(event -> loadInventory());
        closeButton.setOnAction(event -> closeWindow());
    }

    private void loadInventory() {
        List<Item> items = model.getInventoryItems();
        tableView.setItems(FXCollections.observableArrayList(items));

        resultArea.setText("Inventory loaded successfully. Total items: " + items.size());
    }

    @FXML
    private void handleRestock() {
        System.out.println("Restock button clicked!");
    }

    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
