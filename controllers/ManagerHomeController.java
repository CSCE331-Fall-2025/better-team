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

public class ManagerHomeController {
    
    @FXML
    private Button closeButton; //match the fx:id value from Scene Builder

    @FXML
    private Button restockButton;

    @FXML
    private Button orderTrendsButton;

    @FXML
    private Button employeeDataButton;
    
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/CSCE315Database"; //database location
    
    // This method runs automatically when the FXML loads
    @FXML
    public void initialize() {
        // Set up what happens when button is clicked
        closeButton.setOnAction(event -> switchScene("/FXML/Login.fxml"));
        restockButton.setOnAction(event -> switchScene("/FXML/Inventory.fxml"));
        orderTrendsButton.setOnAction(event -> switchScene("/FXML/OrderTrends.fxml"));
        employeeDataButton.setOnAction(event -> switchScene("/FXML/ManagerEmployeeData.fxml"));
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
