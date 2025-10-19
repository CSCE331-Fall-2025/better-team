import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;


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
    @FXML private Button restockNavButton, orderNavButton, employeeNavButton;

	// Init the model for this MVC component
    private final ManagerEmployeeDataModel model = new ManagerEmployeeDataModel();

	
    @FXML
    private void initialize() {
        // Bold the current page on the navbar buttons
        employeeNavButton.setStyle("-fx-font-weight: bold;");

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

    // ----- Navigation stubs (placeholders) -----
    @FXML private void handleNavRestock()  { System.out.println("Navigate: Restock (stub)"); }
    @FXML private void handleNavOrder()    { System.out.println("Navigate: Order Trends (stub)"); }
    @FXML private void handleNavEmployee() { System.out.println("Already on Employee Data"); }
}

