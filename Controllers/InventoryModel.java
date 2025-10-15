package controllers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Database.Item;

import app.dbSetup;

public class InventoryModel {
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/CSCE315Database";

    public List<Item> getInventoryItems() {
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

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }
}
