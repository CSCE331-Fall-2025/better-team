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
    private TextField IDField;
    @FXML
    private Button LoginButton;
    @FXML
    private Button ExitButton;

    private static final String URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/CSCE315Database";
    private dbSetup db = new dbSetup();

    private int EmployeeID = 0;

    @FXML
    public void initialize() {
        LoginButton.setOnAction(e-> LoginButtonHandler());
        ExitButton.setOnAction(e-> closeWindow());
    }

    private void LoginButtonHandler() {

        EmployeeID = tryParseInt(IDField.getText(), 0);
        //ExitButton.setText(String.valueOf(EmployeeID)); //testing
        if(EmployeeID==0){return;}//do nothing if non-int

        //ExitButton.setText(sql); //testing
        dbSetup db = new dbSetup();

        try (Connection conn = DriverManager.getConnection(URL, db.user, db.pswd);
            PreparedStatement ps1 = conn.prepareStatement("SELECT EXISTS (SELECT 1 FROM employee WHERE employee_id = " + EmployeeID + ")");
            ResultSet rs1 = ps1.executeQuery();
            PreparedStatement ps2 = conn.prepareStatement("SELECT ismanager FROM employee WHERE employee_id = " + EmployeeID);
            ResultSet rs2 = ps2.executeQuery();){

            rs1.next();rs2.next();// go to row with the thing I think i couldve done this simpler but idc
            if(rs1.getBoolean(1)){// if exists, try redirect
                boolean ismanager = rs2.getBoolean(1);
                if(ismanager){//if manager->ManagerHome
                    switchScene("/FXML/ManagerHome.fxml");
                }
                else{//if server->ServerOrder
                    switchScene("/FXML/ServerOrder.fxml");
                }
            }

        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }
    
    public int tryParseInt(String value, int def) {
    try { return Integer.parseInt(value); }
    catch (NumberFormatException e) { return def; }
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
