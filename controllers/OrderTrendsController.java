import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
// import javafx.scene.control.TextArea;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;


public class OrderTrendsController {
    
    @FXML   
    private Button closeButton; //match the fx:id value from Scene Builder

    @FXML
    private Button restockButton;

    @FXML
    private Button employeeDataButton;
    
    @FXML
    private TableView<String> dishTable;

    @FXML
    private TableColumn<String, String> dishColumn;
    
    @FXML
    private LineChart<String, Number> dishChart;

    @FXML
    private NumberAxis yAxis;
    
    
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/CSCE315Database"; //database location
    
    // This method runs automatically when the FXML loads
    @FXML
    public void initialize() {
        // Set up what happens when button is clicked
        runQuery();
        closeButton.setOnAction(event -> closeWindow());
        restockButton.setOnAction(event -> switchScene("/FXML/Inventory.fxml"));
        employeeDataButton.setOnAction(event -> switchScene("/FXML/ManagerEmployeeData.fxml"));

        // make table actually show data
        dishColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue())
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
    }
    
    // Your method to run the database query
    private void runQuery() {

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

    private void chartQuery() {

        ObservableList<String> selectedDishes = dishTable.getSelectionModel().getSelectedItems();

        dishChart.getData().clear();

        List<XYChart.Series<String, Number>> allSeries = new ArrayList<>();

        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for(String dishName : selectedDishes) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(dishName);

            try {
                dbSetup my = new dbSetup();
    
                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

                Statement stmt = conn.createStatement();

                String sqlStatement = "SELECT dish.name, COUNT(*) FROM transactiondish JOIN dish on transactiondish.FK_dish = dish.dish_id GROUP BY dish.name HAVING dish.name=?;";
                PreparedStatement pstmt = conn.prepareStatement(sqlStatement);
                pstmt.setString(1, dishName);
                ResultSet rs = pstmt.executeQuery();
                boolean hasData = false;

                while(rs.next()){
                    hasData = true;
                    String name = rs.getString("name");
                    int count = rs.getInt("count");

                    series.getData().add(new XYChart.Data<>(name,count));

                    minY = Math.min(minY, count);
                    maxY = Math.max(maxY, count);
                }

                // dishChart.getData().add(series);

                if(hasData){
                    allSeries.add(series);
                }

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

    private void closeWindow() { 
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

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
}
