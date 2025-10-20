import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;


public class ManagerEmployeeDataController {

    // Employee List Table fx-is;
    @FXML private TableView<ManagerEmployeeDataModel.Employee> employeeListTable;
    @FXML private TableColumn<ManagerEmployeeDataModel.Employee, Integer> employeeIdColumn;
    @FXML private TableColumn<ManagerEmployeeDataModel.Employee, String>  employeeNameColumn;

	// Employee Metric Table fx-ids
    @FXML private TableView<ManagerEmployeeDataModel.EmployeeMetric> employeeMetricTable;
    @FXML private TableColumn<ManagerEmployeeDataModel.EmployeeMetric, String> dateColumn;
    @FXML private TableColumn<ManagerEmployeeDataModel.EmployeeMetric, String> timeColumn;
    @FXML private TableColumn<ManagerEmployeeDataModel.EmployeeMetric, String> costColumn;

    // Nav buttons on the right border to navigate to other manager pages
    @FXML private Button inventoryNavButton, orderTrendsNavButton, employeeDataNavButton;
	@FXML private Button hireButton, fireButton, updateButton;

	// Init the model for this MVC component
    private final ManagerEmployeeDataModel model = new ManagerEmployeeDataModel();

	
    @FXML
    private void initialize() {
        // Bold the current page on the navbar buttons
        employeeDataNavButton.setStyle("-fx-font-weight: bold;");
		
		// nav buttons
		inventoryNavButton.setOnAction(event -> switchScene("/FXML/Inventory.fxml")); 
		orderTrendsNavButton.setOnAction(event -> switchScene("/FXML/OrderTrends.fxml"));
		employeeDataNavButton.setOnAction(event -> switchScene("/FXML/ManagerEmployeeData.fxml"));

		// Hire, fire, manage employee buttons via fxml dialogs
		hireButton.setOnAction(e -> onHire());
		fireButton.setOnAction(e -> onFire());
		updateButton.setOnAction(e -> onUpdate());

        // Wire columns to getters (PropertyValueFactory looks for getXxx())
        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));     // getId()
        employeeNameColumn.setCellValueFactory(new PropertyValueFactory<>("name")); // getName()

        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));         // getDate()
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));         // getTime()
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));         // getCost()

        // Load employees
        ObservableList<ManagerEmployeeDataModel.Employee> employees = model.getAllEmployees();
        employeeListTable.setItems(employees);
        employeeListTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // When an employee is selected, load their transactions
        employeeListTable.getSelectionModel().selectedItemProperty().addListener((obs, oldEmp, newEmp) -> {
            if (newEmp != null) {
                employeeMetricTable.setItems(model.getMetricsForEmployee(newEmp.getId()));
            } else {
                employeeMetricTable.getItems().clear();
            }
        });

        // Optional: auto-select first employee
        if (!employees.isEmpty()) {
            employeeListTable.getSelectionModel().selectFirst();
        }

        // Optional niceties
        employeeListTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        employeeMetricTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

	// Scene Switch Helper
	private void switchScene(String fxmlPath) {
    	try {
        	FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        	Parent root = loader.load();

        	Stage stage = (Stage) inventoryNavButton.getScene().getWindow();
        	stage.setScene(new Scene(root));
        	stage.show();
    	} catch (Exception e) {
        	e.printStackTrace();
    	}
	}
	// --- Hire new employee ---
	@FXML
	private void onHire() {
		TextInputDialog nameDialog = new TextInputDialog();
		nameDialog.setTitle("Hire Employee");
		nameDialog.setHeaderText("Add New Employee");
		nameDialog.setContentText("Enter employee name:");
		var nameOpt = nameDialog.showAndWait();
		if (nameOpt.isEmpty() || nameOpt.get().trim().isEmpty()) return;

		TextInputDialog managerDialog = new TextInputDialog("false");
		managerDialog.setTitle("Hire Employee");
		managerDialog.setHeaderText("Is this employee a manager? (true/false)");
		managerDialog.setContentText("Manager status:");
		var managerOpt = managerDialog.showAndWait();
		if (managerOpt.isEmpty()) return;

		boolean isManager = Boolean.parseBoolean(managerOpt.get().trim());

		TextInputDialog wageDialog = new TextInputDialog("10");
		wageDialog.setTitle("Hire Employee");
		wageDialog.setHeaderText("Set wage for " + nameOpt.get());
		wageDialog.setContentText("Enter wage:");
		var wageOpt = wageDialog.showAndWait();
		if (wageOpt.isEmpty()) return;

		try {
			double wage = Double.parseDouble(wageOpt.get().trim());
			model.addEmployee(nameOpt.get().trim(), isManager, wage);
			refreshEmployeeList();
		} catch (NumberFormatException e) {
			showAlert(Alert.AlertType.ERROR, "Invalid Input", "Wage must be a valid number.");
		}
	}

	

	// --- Fire selected employee ---
	@FXML
	private void onFire() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Fire Employee");
		dialog.setHeaderText("Remove Employee");
		dialog.setContentText("Enter employee ID or name:");
		dialog.showAndWait().ifPresent(input -> {
			ManagerEmployeeDataModel.Employee emp = model.getEmployeeByIdOrName(input.trim());
			if (emp == null) {
				showAlert(Alert.AlertType.WARNING, "Not Found", "No employee found with that name or ID.");
				return;
			}
			Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
			confirm.setTitle("Confirm Fire");
			confirm.setHeaderText("Fire " + emp.getName() + "?");
			confirm.showAndWait().ifPresent(resp -> {
				if (resp == ButtonType.OK) {
					model.removeEmployee(emp.getId());
					refreshEmployeeList();
				}
			});
		});
	}

	// --- Update selected employee name ---
	@FXML
	private void onUpdate() {
		TextInputDialog searchDialog = new TextInputDialog();
		searchDialog.setTitle("Update Employee");
		searchDialog.setHeaderText("Enter ID or name of the employee to update:");
		searchDialog.setContentText("Employee ID or Name:");
		var searchOpt = searchDialog.showAndWait();
		if (searchOpt.isEmpty()) return;

		ManagerEmployeeDataModel.Employee emp = model.getEmployeeByIdOrName(searchOpt.get().trim());
		if (emp == null) {
			showAlert(Alert.AlertType.WARNING, "Not Found", "No employee found with that ID or name.");
			return;
		}

		TextInputDialog nameDialog = new TextInputDialog(emp.getName());
		nameDialog.setTitle("Update Employee");
		nameDialog.setHeaderText("Edit Employee Name");
		nameDialog.setContentText("New name:");
		var nameOpt = nameDialog.showAndWait();
		if (nameOpt.isEmpty()) return;
		String newName = nameOpt.get().trim();

		TextInputDialog managerDialog = new TextInputDialog("false");
		managerDialog.setTitle("Update Employee");
		managerDialog.setHeaderText("Set manager status (true/false)");
		managerDialog.setContentText("Is Manager:");
		boolean isManager = Boolean.parseBoolean(managerDialog.showAndWait().orElse("false").trim());

		TextInputDialog wageDialog = new TextInputDialog("10");
		wageDialog.setTitle("Update Employee");
		wageDialog.setHeaderText("Set new wage");
		wageDialog.setContentText("Wage:");
		try {
			double wage = Double.parseDouble(wageDialog.showAndWait().orElse("10").trim());
			model.updateEmployee(emp.getId(), newName, isManager, wage);
			refreshEmployeeList();
		} catch (NumberFormatException e) {
			showAlert(Alert.AlertType.ERROR, "Invalid Input", "Wage must be a valid number.");
		}
	}



	// --- Small utility helpers ---
	private void refreshEmployeeList() {
		employeeListTable.setItems(model.getAllEmployees());
	}

	private void showAlert(Alert.AlertType type, String title, String msg) {
		Alert a = new Alert(type);
		a.setTitle(title);
		a.setHeaderText(null);
		a.setContentText(msg);
		a.showAndWait();
	}

}

