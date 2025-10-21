import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;


/**
 * Controller for the order trends page.
 */
public class OrderTrendsController {
    
    @FXML   
    private Button closeButton; //match the fx:id value from Scene Builder

    @FXML
    private Button restockButton;

    @FXML
    private Button employeeDataButton;

    @FXML
    private Button dishButton;

    @FXML
    private Button inventoryButton;
    
    @FXML
    private Button xReportButton;

    @FXML
    private Button zReportButton;
    
    @FXML
    private TableView<String> dishTable;

    @FXML
    private TableColumn<String, String> dishColumn;

    @FXML
    private TableView<ReportData> reportTable;
    
    @FXML
    private TableColumn<ReportData, String> timeCol;

    @FXML
    private TableColumn<ReportData, String> transactionCol;

    @FXML
    private TableColumn<ReportData, String> salesCol;
    
    @FXML
    private LineChart<String, Number> dishChart;

    @FXML
    private NumberAxis yAxis;
    
    @FXML
    private DatePicker dateStart;
    
    @FXML
    private DatePicker dateEnd;

    @FXML
    private Label time;
    
    
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/CSCE315Database"; //database location

    private boolean displayDishView = true;
    
    // This method runs automatically when the FXML loads
    /**
     * Everything that runs when the page is first loaded. Buttons/Tables/Time are enabled.
     */
    @FXML
    public void initialize() {
        // Set up what happens when button is clicked
        dishQuery();
        closeButton.setOnAction(event -> switchScene("/FXML/Login.fxml"));
        restockButton.setOnAction(event -> switchScene("/FXML/Inventory.fxml"));
        employeeDataButton.setOnAction(event -> switchScene("/FXML/ManagerEmployeeData.fxml"));

        // make table actually show data
        dishColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue())
        );       

        timeCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTime())
        );

        transactionCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getTransactions()))
        );

        salesCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(String.format("$%.2f",cellData.getValue().getSales()))
        );
        
        // allow multiple cells to be selected
        dishTable.getSelectionModel().setSelectionMode(
            SelectionMode.MULTIPLE
        );

        // make chart show data from table
        dishTable.getSelectionModel().getSelectedItems().addListener(
            (ListChangeListener.Change<? extends String> change) -> {
                chartQuery();
            }
        );

        dateStart.setValue(LocalDate.of(2024,9,22));
        dateEnd.setValue(LocalDate.of(2024,9,23));

        dateStart.setOnAction(event -> chartQuery());
        dateEnd.setOnAction(event -> chartQuery());

        dishButton.setOnAction(event -> dishQuery());
        inventoryButton.setOnAction(event -> inventoryQuery());

        xReportButton.setOnAction(event -> reportQuery(false));
        zReportButton.setOnAction(event -> reportQuery(true));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm:ss a");
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            String currTime = LocalDateTime.now().format(formatter);
            time.setText(currTime);
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    // Your method to run the database query
    /**
     * Fills the leftmost table with dishes.
     */
    private void dishQuery() {

        try {
            // Get database creditials
            dbSetup my = new dbSetup();
 
            // Build the connection
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            // Create statement
            Statement stmt = conn.createStatement();

            // Run sql query
            String sqlStatement = "SELECT name FROM dish";
            ResultSet rs = stmt.executeQuery(sqlStatement);

            ObservableList<String> dishList = FXCollections.observableArrayList();

            // Output result to table
            while (rs.next()){
                dishList.add(rs.getString("name"));
            }

            dishTable.setItems(dishList);
            dishColumn.setText("Dish");
            displayDishView = true;

            // Close connection
            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            // resultArea.setText("Error connecting to database:\n" + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Fills the leftmost table with inventory.
     */
    private void inventoryQuery() {

        try {
            // Get database creditials
            dbSetup my = new dbSetup();
 
            // Build the connection
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            // Create statement
            Statement stmt = conn.createStatement();

            // Run sql query
            String sqlStatement = "SELECT item FROM inventory";
            ResultSet rs = stmt.executeQuery(sqlStatement);

            ObservableList<String> inventoryList = FXCollections.observableArrayList();

            // Output result to table
            while (rs.next()){
                inventoryList.add(rs.getString("item"));
            }

            dishTable.setItems(inventoryList);
            dishColumn.setText("Inventory");
            displayDishView = false;

            // Close connection
            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            // resultArea.setText("Error connecting to database:\n" + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Fills the main chart with data selected in the leftmost table.
     */
    private void chartQuery() {

        ObservableList<String> selectedItems = dishTable.getSelectionModel().getSelectedItems();

        LocalDate startDate = dateStart.getValue();
        LocalDate endDate = dateEnd.getValue();

        dishChart.getData().clear();

        if(startDate == null || endDate == null || selectedItems.isEmpty()){
            yAxis.setAutoRanging(true);
            return;
        }

        List<XYChart.Series<String, Number>> allSeries = new ArrayList<>();

        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for(String itemName : selectedItems) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(itemName);

            try {
                dbSetup my = new dbSetup();
    
                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

                // Statement stmt = conn.createStatement();

                String sqlStatement;

                if(displayDishView){
                    sqlStatement = "SELECT DATE(t.time) as sale_date, COUNT(*) as count FROM transactiondish td JOIN dish d ON td.FK_dish = d.dish_id JOIN transaction t ON td.FK_transaction = t.transaction_id WHERE d.name = ? AND DATE(t.time) >= ? AND DATE(t.time) <= ? GROUP BY sale_date ORDER BY sale_date;";
                }
                else{
                    sqlStatement = "SELECT DATE(t.time) AS sale_date, COUNT(t.transaction_id) AS count FROM transaction t JOIN transactiondish td ON t.transaction_id = td.fk_transaction JOIN dish d ON td.fk_dish = d.dish_id JOIN dishinventory di ON d.dish_id = di.fk_dish JOIN inventory i ON di.fk_inventory = i.inventory_id WHERE i.item = ? AND DATE(t.time) >= ? AND DATE(t.time) <= ? GROUP BY sale_date ORDER BY sale_date;";
                }

                PreparedStatement pstmt = conn.prepareStatement(sqlStatement);
                pstmt.setString(1, itemName);
                pstmt.setDate(2, java.sql.Date.valueOf(startDate));
                pstmt.setDate(3, java.sql.Date.valueOf(endDate));

                ResultSet rs = pstmt.executeQuery();
                boolean hasData = false;

                while(rs.next()){
                    hasData = true;
                    // String name = rs.getString("name");
                    String saleDate = rs.getDate("sale_date").toString();
                    int count = rs.getInt("count");

                    series.getData().add(new XYChart.Data<>(saleDate,count));

                    minY = Math.min(minY, count);
                    maxY = Math.max(maxY, count);
                }

                // dishChart.getData().add(series);

                if(hasData){
                    allSeries.add(series);
                }

                // Close connection
                rs.close();
                pstmt.close();
                conn.close();

            } catch (Exception e) {
                // resultArea.setText("Error connecting to database:\n" + e.getMessage());
                e.printStackTrace();
                System.exit(0);
            }
        }

        if(!allSeries.isEmpty()){
            yAxis.setAutoRanging(false);

            double pad = (maxY - minY) * 0.1;

            yAxis.setLowerBound(minY - pad);
            yAxis.setUpperBound(maxY + pad);

            dishChart.getData().addAll(allSeries);
        }
        else{
            yAxis.setAutoRanging(true);
        }
    }

    /**
     * Fills the rightmost table with a current day report of sales.
     * @param isZ is the report a Z report? If not then it's an X report.
     */
    private void reportQuery(boolean isZ){

        LocalDate reportDate = LocalDate.now();

        try {
            // Get database creditials
            dbSetup my = new dbSetup();
 
            // Build the connection
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            ObservableList<ReportData> reportDataList = FXCollections.observableArrayList();
            // Run sql query
            String sqlStatement;

            if(!isZ){
                sqlStatement = "SELECT EXTRACT(HOUR FROM t.time) AS hour_of_day, COUNT(t.transaction_id) as total_transactions, SUM(t.cost) AS total_sales FROM transaction t WHERE DATE(t.time) = ? GROUP BY hour_of_day ORDER BY hour_of_day;";
                
                PreparedStatement pstmt = conn.prepareStatement(sqlStatement);
                pstmt.setDate(1, java.sql.Date.valueOf(reportDate));
                ResultSet rs = pstmt.executeQuery();

                // Output result to table
                while (rs.next()){
                    int hour = rs.getInt("hour_of_day");
                    String hourRange = String.format("%02d:00 - %02d:59", hour, hour);
                    int transactions = rs.getInt("total_transactions");
                    double sales = rs.getDouble("total_sales");

                    reportDataList.add(new ReportData(hourRange, transactions, sales));
                }

                // Close connection
                rs.close();
                pstmt.close();
            }
            else{
                sqlStatement = "SELECT COUNT(t.transaction_id) AS total_transactions, SUM(t.cost) AS total_sales FROM transaction t WHERE DATE(t.time) = ?;";
            
                PreparedStatement pstmt = conn.prepareStatement(sqlStatement);
                pstmt.setDate(1, java.sql.Date.valueOf(reportDate));
                ResultSet rs = pstmt.executeQuery();

                // Output result to table
                while (rs.next()){
                    int transactions = rs.getInt("total_transactions");
                    double sales = rs.getDouble("total_sales");

                    reportDataList.add(new ReportData(reportDate.toString(), transactions, sales));
                }

                // Close connection
                rs.close();
                pstmt.close();
            }


            reportTable.setItems(reportDataList); 

            conn.close();

        } catch (Exception e) {
            // resultArea.setText("Error connecting to database:\n" + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Switches to the next javafx scene.
     * @param fileName name of the .fxml file being switched too, path included if necessary.
     */
    private void switchScene(String fileName){
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fileName));
            Stage stage = (Stage) closeButton.getScene().getWindow();
            double currWidth = stage.getScene().getWidth();            
            double currHeight = stage.getScene().getHeight();
            Scene scene = new Scene(root, currWidth, currHeight);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Failed to switch scene: " + fileName);
            e.printStackTrace();
            System.exit(0);
        }
        
    }

    /**
     * Holds data for the rightmost table the report table.
     * Consists of time, number of interactions, and total sales in dollars.
     */
    public static class ReportData{
        private final String time;
        private final int transactions;
        private final double sales;

        /**
         * ReportData Constructor.
         * @param time string of the hour, formatted XX:00 - XX:59.
         * @param transactions total number of transactions in that hour.
         * @param sales total number of sales, in dollars, in that hour.
         */
        public ReportData(String time, int transactions, double sales){
            this.time = time;
            this.transactions = transactions;
            this.sales = sales;
        }


        /**
         * Getter for the time string from ReportData. 
         * @return String of the hour, formatted XX:00 - XX:59.
         */
        public String getTime() {return time;}
        /**
         * Getter for the total transactions from ReportData. 
         * @return int of transactions in that hour.
         */
        public int getTransactions() {return transactions;}
        /**
         * Getter for the total sales from ReportData. 
         * @return double of sales, in dollars, in that hour.
         */
        public double getSales() {return sales;}
    }
}

