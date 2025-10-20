import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.sql.*;
import java.util.List;

import app.dbSetup;

public class CheckoutController {

    @FXML private TextArea orderTextArea;
    @FXML private TextArea notesTextArea;
    @FXML private Button cancelButton;
    @FXML private Button addNotesButton;
    @FXML private Button payButton;

    private List<Integer> selectedItems;

    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/CSCE315Database";
    private dbSetup db = new dbSetup();

    @FXML
    public void initialize(){

        notesTextArea.setEditable(false);
        notesTextArea.setPromptText("Click 'Add Notes' to write a note...");


        cancelButton.setOnAction(e -> switchSceneSafe(e, "/FXML/ServerOrder.fxml"));
        payButton.setOnAction(this::processPayment);
        addNotesButton.setOnAction(e -> {
            boolean wasEditable = notesTextArea.isEditable();
            notesTextArea.setEditable(!wasEditable);

            if(notesTextArea.isEditable()){
                notesTextArea.setPromptText("Type notes here...");
                notesTextArea.requestFocus();
                addNotesButton.setText("Lock Notes");
            }else{
                notesTextArea.setPromptText("Notes locked.");
                addNotesButton.setText("Add Notes");
            }
        });
    }

    public void setSelectedItems(List<Integer> selectedItems){
        this.selectedItems = selectedItems;
        loadCurrentOrder();
    }

    private void loadCurrentOrder(){
        if (selectedItems == null || selectedItems.isEmpty()) {
            orderTextArea.setText("No items in order!");
            return;
        }

        StringBuilder orderDisplay = new StringBuilder();
        double total = 0;

        try(Connection conn = DriverManager.getConnection(DB_URL, db.user, db.pswd)){
            for(int code : selectedItems){
                int dishId = code / 100;
                int qty = code % 100;

                try(PreparedStatement stmt = conn.prepareStatement("SELECT name, price FROM dish WHERE dish_id = ?")){
                    stmt.setInt(1, dishId);
                    ResultSet rs = stmt.executeQuery();

                    if(rs.next()){
                        String name = rs.getString("name");
                        double price = rs.getDouble("price");

                        orderDisplay.append(name)
                            .append(" x").append(qty)
                            .append(" - $").append(String.format("%.2f", price * qty))
                            .append("\n");

                        total += price * qty;
                    }else{
                        orderDisplay.append("Dish ID ").append(dishId).append(" not found.\n");
                    }
                }
            }

            orderDisplay.append("\nTOTAL: $").append(String.format("%.2f", total));
            orderTextArea.setText(orderDisplay.toString());

        }catch(SQLException e){
            e.printStackTrace();
            orderTextArea.setText("Error loading order data:\n" + e.getMessage());
        }
    }

private void processPayment(ActionEvent event){
    if(selectedItems == null || selectedItems.isEmpty()){
        orderTextArea.setText("No items to pay for!");
        return;
    }

    try(Connection conn = DriverManager.getConnection(DB_URL, db.user, db.pswd)){
        conn.setAutoCommit(false);

        int lastId = 0;
        try(Statement stmt = conn.createStatement()){
            ResultSet rs = stmt.executeQuery("SELECT transaction_id FROM transaction ORDER BY transaction_id DESC LIMIT 1");
            if(rs.next()){
                lastId = rs.getInt("transaction_id");
            }
        }
        int transactionId = lastId + 1;

        String insertTransaction = "INSERT INTO transaction (transaction_id, fk_customer, fk_employee, cost, time) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try(PreparedStatement txStmt = conn.prepareStatement(insertTransaction)){
            txStmt.setInt(1, transactionId);
            txStmt.setInt(2, 14);
            txStmt.setInt(3, 15);
            txStmt.setInt(4, 0);
            txStmt.executeUpdate();
        }

        String insertDish = "INSERT INTO transactiondish (fk_transaction, fk_dish) VALUES (?, ?)";
        try(PreparedStatement tdStmt = conn.prepareStatement(insertDish)){
            for(int code : selectedItems){
                int dishId = code / 100;
                tdStmt.setInt(1, transactionId);
                tdStmt.setInt(2, dishId);
                tdStmt.addBatch();
            }
            tdStmt.executeBatch();
        }

        updateInventory(conn);

        conn.commit();

        selectedItems.clear();
        orderTextArea.setText("Transaction complete!\nID: " + transactionId);
        switchSceneSafe(event, "/FXML/ServerOrder.fxml");

    }catch(SQLException e){
        e.printStackTrace();
        orderTextArea.setText("Payment failed:\n" + e.getMessage());
    }
}

    private void updateInventory(Connection conn) throws SQLException{
        String getInventory = "SELECT di.fk_inventory, COUNT(*) AS used FROM dishinventory di WHERE di.fk_dish = ? GROUP BY di.fk_inventory";

        String updateInventory = "UPDATE inventory SET current_inventory = current_inventory - ? WHERE inventory_id = ?";

        try(PreparedStatement invStmt = conn.prepareStatement(getInventory);PreparedStatement updStmt = conn.prepareStatement(updateInventory)){
            for(int code : selectedItems){
                int dishId = code / 100;
                int qty = code % 100;

                invStmt.setInt(1, dishId);
                ResultSet rs = invStmt.executeQuery();

                while(rs.next()){
                    int inventoryId = rs.getInt("fk_inventory");
                    int usedPerDish = rs.getInt("used");

                    updStmt.setInt(1, usedPerDish * qty);
                    updStmt.setInt(2, inventoryId);
                    updStmt.addBatch();
                }
            }
            updStmt.executeBatch();
        }
    }

    private void switchSceneSafe(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading " + fxmlFile + ": " + e.getMessage());
        }
    }
}