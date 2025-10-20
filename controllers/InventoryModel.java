// Handles all database operations (SQL queries) related to inventory and dishes.
// This is your Data Access Object (DAO) layer.

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
* Handles all database operations (SQL queries) related to inventory items.
* Serves as the Data Access Object (DAO) layer for inventory management.
*/
public class InventoryModel {
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/CSCE315Database";

    /**
    * Retrieves all inventory items from the database.
    *
    * @return a list of {@link InventoryController.Item} representing the inventory
    * @throws RuntimeException if there is a database access error
    */
    public List<InventoryController.Item> getInventoryItems() {
        List<InventoryController.Item> items = new ArrayList<>();
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
                items.add(new InventoryController.Item(name, current, recommended));
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }
}
