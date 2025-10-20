import java.sql.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javafx.collections.ObservableList;

public class LoginController {

    @FXML
    private Button ServerButton;
    @FXML
    private Button ManagerButton;
    @FXML
    private Button ExitButton;

    @FXML
    public void initialize() {
        ServerButton.setOnAction(e-> switchScene("../FXML/ServerOrder.fxml"));
        ManagerButton.setOnAction(e-> switchScene("ManagerHome.fxml"));
        ExitButton.setOnAction(e-> closeWindow());
    }
    
    
    private void switchScene(String fileName){
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fileName));
            Stage stage = (Stage) ExitButton.getScene().getWindow();
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

    private void closeWindow() { 
        Stage stage = (Stage) ExitButton.getScene().getWindow();
        stage.close();
    }
}