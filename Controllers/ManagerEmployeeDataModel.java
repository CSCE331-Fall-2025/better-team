package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.math.BigDecimal;

public class ManagerEmployeeDataModel {

    // ---- JDBC connection config  ----
    private static final String URL  = "jdbc:postgresql://csce-315-db.engr.tamu.edu/CSCE315Database";
    private static final String USER = "better_team";
    private static final String PASS = "12347";



	// Employee object to create instances of from the queyr results
    public static class Employee {
        private final int id;
        private final String name;

        public Employee(int id, String name) { this.id = id; this.name = name; }
        public int getId()   { return id; }
        public String getName(){ return name; }
    }


	// Employee Metric object to create instances of from the query
    public static class EmployeeMetric {
        private final String date;
        private final String time;
        private final String cost;

        public EmployeeMetric(String date, String time, String cost) {
            this.date = date;
            this.time = time;
            this.cost = cost;
        }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getCost() { return cost; }
    }


	/**
	 * Method to get Employee data for the ManagerEmployeeDataView
	 * @return an ObservableList<Employee> 
	 */
    public ObservableList<Employee> getAllEmployees() {
        ObservableList<Employee> out = FXCollections.observableArrayList();

        final String sql = "SELECT employee_id, name FROM employee ORDER BY employee_id";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(new Employee(
                    rs.getInt("employee_id"),
                    rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

	// Method to get Employee & respective transaciton info
    public ObservableList<EmployeeMetric> getMetricsForEmployee(int employeeId) {
        ObservableList<EmployeeMetric> out = FXCollections.observableArrayList();

		// We were dumb and our table names include SQL keywords (transaction, time), so special escape char
        final String sql =
            "SELECT to_char(t.\"time\", 'YYYY-MM-DD') AS tx_date, " +
            "       to_char(t.\"time\", 'HH24:MI:SS') AS tx_time, " +
            "       to_char(t.cost,    'FM9999990.00') AS tx_cost " +
            "FROM \"transaction\" t " +
            "WHERE t.fk_employee = ? " +
            "ORDER BY t.\"time\" DESC";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("tx_date");
                    String time = rs.getString("tx_time");
                    String cost = rs.getString("tx_cost");
                    out.add(new EmployeeMetric(date, time, cost));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return out;
    }
}


