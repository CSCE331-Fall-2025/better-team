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

/**
 * Controller for the checkout screen of the ordering system.
 * Handles displaying the current order, adding notes, processing payments,
 * and switching between scenes in the JavaFX interface.
 * This class also connects to a PSQL database to log transactions and
 * update inventory data when payments are processed.
 */
public class CheckoutController {

    /** Text area displaying the current order. */
    @FXML private TextArea orderTextArea;

    /** Text area for customer or employee notes about the order. */
    @FXML private TextArea notesTextArea;

    /** Button that cancels the checkout and returns to the order screen. */
    @FXML private Button cancelButton;

    /** Button that toggles if notes can be edited or not */
    @FXML private Button addNotesButton;

    /** Button that processes the payment and completes the transaction. */
    @FXML private Button payButton;

    /** List of encoded integers representing the items in the order. */
    private List<Integer> selectedItems;

    /** JDBC connection to the PostgreSQL database. */
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/CSCE315Database";

    /** Database credential containing username and password. */
    private dbSetup db = new dbSetup();

    /** Stores the current total cost of the order. */
    private double total = 0;

    /**
     * Initializes the checkout screen components.
     * Sets up button event handlers for canceling, adding notes, and paying.
     * Disables editing of order text areas
     */
    @FXML
    public void initialize() {
        orderTextArea.setEditable(false);
        notesTextArea.setEditable(false);
        notesTextArea.setPromptText("Click 'Add Notes' to write a note...");

        cancelButton.setOnAction(e -> switchSceneSafe(e, "/FXML/ServerOrder.fxml"));
        payButton.setOnAction(this::processPayment);

        addNotesButton.setOnAction(e -> {
            boolean wasEditable = notesTextArea.isEditable();
            notesTextArea.setEditable(!wasEditable);

            if (notesTextArea.isEditable()) {
                notesTextArea.setPromptText("Type notes here...");
                notesTextArea.requestFocus();
                addNotesButton.setText("Lock Notes");
            } else {
                notesTextArea.setPromptText("Notes locked.");
                addNotesButton.setText("Add Notes");
            }
        });
    }

    /**
     * Sets the list of selected item codes for the current order and loads their details.
     *
     * @param selectedItems a list of encoded integers representing dishes and quantities
     */
    public void setSelectedItems(List<Integer> selectedItems) {
        this.selectedItems = selectedItems;
        loadCurrentOrder();
    }

    /**
     * Loads and displays the details of the current order in the {@link #orderTextArea}.
     * Each integer in {@code selectedItems} encodes the dish ID and quantity as:
     * {@code (dishId * 100 + quantity)}.
     * Queries the database for item names and prices, then calculates and displays the total.
     */
    private void loadCurrentOrder() {
        if (selectedItems == null || selectedItems.isEmpty()) {
            orderTextArea.setText("No items in order!");
            return;
        }

        StringBuilder orderDisplay = new StringBuilder();
        total = 0;

        try (Connection conn = DriverManager.getConnection(DB_URL, db.user, db.pswd)) {
            for (int code : selectedItems) {
                int dishId = code / 100;
                int qty = code % 100;

                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT name, price FROM dish WHERE dish_id = ?")) {
                    stmt.setInt(1, dishId);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String name = rs.getString("name");
                        double price = rs.getDouble("price");

                        orderDisplay.append(name)
                                .append(" x").append(qty)
                                .append(" - $").append(String.format("%.2f", price * qty))
                                .append("\n");

                        total += price * qty;
                    } else {
                        orderDisplay.append("Dish ID ").append(dishId).append(" not found.\n");
                    }
                }
            }

            orderDisplay.append("\nTOTAL: $").append(String.format("%.2f", total));
            orderTextArea.setText(orderDisplay.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            orderTextArea.setText("Error loading order data:\n" + e.getMessage());
        }
    }

    /**
     * Processes payment for the current order.
     * Creates a new transaction record and associates it with each ordered dish.
     * Updates the database inventory accordingly and resets the order.
     *
     * @param event the {@link ActionEvent} triggered by clicking the Pay button
     */
    private void processPayment(ActionEvent event) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            orderTextArea.setText("No items to pay for!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, db.user, db.pswd)) {
            conn.setAutoCommit(false);

            int lastId = 0;
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                        "SELECT transaction_id FROM transaction ORDER BY transaction_id DESC LIMIT 1");
                if (rs.next()) {
                    lastId = rs.getInt("transaction_id");
                }
            }
            int transactionId = lastId + 1;

            String insertTransaction =
                    "INSERT INTO transaction (transaction_id, fk_customer, fk_employee, cost, time) " +
                    "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement txStmt = conn.prepareStatement(insertTransaction)) {
                txStmt.setInt(1, transactionId);
                txStmt.setInt(2, 14);
                txStmt.setInt(3, 15);
                txStmt.setDouble(4, total);
                txStmt.executeUpdate();
            }

            String insertDish =
                    "INSERT INTO transactiondish (fk_transaction, fk_dish) VALUES (?, ?)";
            try (PreparedStatement tdStmt = conn.prepareStatement(insertDish)) {
                for (int code : selectedItems) {
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

        } catch (SQLException e) {
            e.printStackTrace();
            orderTextArea.setText("Payment failed:\n" + e.getMessage());
        }
    }

    /**
     * Updates inventory quantities in the database based on items sold.
     * Decrements inventory counts for each item used in the order.
     *
     * @param conn the open SQL connection used for this transaction
     * @throws SQLException if a database update error occurs
     */
    private void updateInventory(Connection conn) throws SQLException {
        String getInventory =
                "SELECT di.fk_inventory, COUNT(*) AS used " +
                "FROM dishinventory di WHERE di.fk_dish = ? GROUP BY di.fk_inventory";

        String updateInventory =
                "UPDATE inventory SET current_inventory = current_inventory - ? WHERE inventory_id = ?";

        try (PreparedStatement invStmt = conn.prepareStatement(getInventory);
             PreparedStatement updStmt = conn.prepareStatement(updateInventory)) {

            for (int code : selectedItems) {
                int dishId = code / 100;
                int qty = code % 100;

                invStmt.setInt(1, dishId);
                ResultSet rs = invStmt.executeQuery();

                while (rs.next()) {
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

    /**
     * Safely switches to another FXML scene, preserving stage state.
     *
     * @param event the {@link ActionEvent} that triggered the scene switch
     * @param fxmlFile the relative path to the FXML file to load
     */
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
