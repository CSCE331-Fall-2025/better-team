import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.math.BigDecimal;

/**
 * Model for the ManagerEmployeeData.fxml file/view.
 *
 * <p>
 * Contains subclasses of Employee and EmployeeMetric to be instatiated for each entry of the employee table and
 * transaction data. Ultimately using a collection of the objects to be referenced when displaying table information.
 * 
 * Contains getter functions to query the databse for the employee and employeeMetric objects
 * 
 * Backemd implementation to update the database when hiring, firing, or editing employees.
 */
public class ManagerEmployeeDataModel {

    // ---- JDBC connection config  ----
    private static final String URL  = "jdbc:postgresql://csce-315-db.engr.tamu.edu/CSCE315Database";

	// Employee object to create instances of from the queyr results
	public static class Employee {
		private final int id;
		private final String name;
		private final double wage;

		public Employee(int id, String name, double wage) {
			this.id = id;
			this.name = name;
			this.wage = wage;
		}

		/**
		 * Employe ID Getter
		 *
		 * @return Employee ID as int
		 */
		public int getId() { return id; }

		/**
		 * Employee Name Getter
		 * @return Employee name as string
		 */
		public String getName() { return name; }

		/**
		 * Employee Wage Getter
		 * @return Employee wage as double
		 */
		public double getWage() { return wage; }
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

		/**
		 * Employee Metric Date Getter
		 * @return Date of transaction as a string
		 */
        public String getDate() { return date; }

		/**
		 * Employee Metric Time Getter
		 * @return Time of transaction as a string
		 */
        public String getTime() { return time; }

		/**
		 * Employee Metric Transaction Cost Getter
		 * @return Cost of transaction as a String
		 */
        public String getCost() { return cost; }
    }


	/**
	 * Method to get Employee data for the ManagerEmployeeDataView
	 *
	 * <p>
	 * Connects to DB, parses query result, instatiates a Employee Object, and adds to List of Employees
	 * @return an ObservableList of Employee Objects 
	 */
    public ObservableList<Employee> getAllEmployees() {
        ObservableList<Employee> out = FXCollections.observableArrayList();

        final String sql = "SELECT employee_id, name, wage FROM employee ORDER BY employee_id";
		dbSetup db = new dbSetup();

        try (Connection conn = DriverManager.getConnection(URL, db.user, db.pswd);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(new Employee(
                    rs.getInt("employee_id"),
                    rs.getString("name"),
					rs.getDouble("wage")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

	/**
	 * Method to get Employee Metric data for the ManagerEmployeeDataView
	 *
	 * <p>
	 * Connects to DB, parses query result, instatiates a EmployeeMetric Object, and adds to List of EmployeeMetrics
	 * @return an ObservableList of EmployeeMetric Objects
	 */
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

		dbSetup db = new dbSetup();

        try (Connection conn = DriverManager.getConnection(URL, db.user, db.pswd);
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
	/**
	 * Updates the DB with a new Employee (hire)
	 * 
	 * <p>
	 * The backend implementation to update the DB from the data collected in the dialog options from the 
	 * hire button.
	 *
	 * @param name Employee's name as String
	 * @param isManager Boolean value for Manager Status (true for manager)
	 * @param wage Double value for the wage of the Employee
	 */
	public void addEmployee(String name, boolean isManager, double wage) {
		final String sql = "INSERT INTO employee (name, ismanager, wage) VALUES (?, ?, ?)";
		dbSetup db = new dbSetup();
		try (Connection conn = DriverManager.getConnection(URL, db.user, db.pswd);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, name);
			ps.setBoolean(2, isManager);
			ps.setDouble(3, wage);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates the DB and removes an Employee (fire)
	 * 
	 * <p>
	 * The backend implementation to update the DB from the data collected in the dialog options from the 
	 * fire button.
	 *
	 * @param id The to be removed employee's ID
	 */
	public void removeEmployee(int id) {
		final String sql = "DELETE FROM employee WHERE employee_id = ?";
		dbSetup db = new dbSetup();
		try (Connection conn = DriverManager.getConnection(URL, db.user, db.pswd);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates the DB to reflect editing an Employees Information (update)
	 * 
	 * <p>
	 * The backend implementation to update the DB from the data collected in the dialog options from the 
	 * update button.
	 *
	 * @param id ID of Employee as int
	 * @param newName The "to be" name for the updated employee information
	 * @param isMangager Boolean value for the type of employee (manager vs cashier); true for manager
	 * @param wage Double value for the updated wage of the Employee
	 */
	public void updateEmployee(int id, String newName, boolean isManager, double wage) {
		final String sql = "UPDATE employee SET name = ?, ismanager = ?, wage = ? WHERE employee_id = ?";
		dbSetup db = new dbSetup();
		try (Connection conn = DriverManager.getConnection(URL, db.user, db.pswd);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, newName);
			ps.setBoolean(2, isManager);
			ps.setDouble(3, wage);
			ps.setInt(4, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Helper function to identify employee by name or employee_id
	 *
	 * @param input The String that the manager typed in when identifying employee by name or id
	 * @return An Employee Object from the ManagerEmployeeDataModel's Inner Employee Class. 
	 */
	public ManagerEmployeeDataModel.Employee getEmployeeByIdOrName(String input) {
		// Include wage in the query
		final String sql = "SELECT employee_id, name, wage FROM employee " +
						   "WHERE employee_id::text = ? OR LOWER(name) = LOWER(?)";

		dbSetup db = new dbSetup();

		try (Connection conn = DriverManager.getConnection(URL, db.user, db.pswd);

			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, input);
			ps.setString(2, input);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return new Employee(
					rs.getInt("employee_id"),
					rs.getString("name"),
					rs.getDouble("wage")
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}


