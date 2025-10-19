import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;


import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;

import javafx.stage.Stage;

import javafx.event.ActionEvent;

public class ServerAppController {

    /*public void setSelectedItems(List<Integer> selectedItems) {
        this.selectedItems = selectedItems;
    }*/

    //apps
    @FXML
    private Button ChickenRoll;
    @FXML
    private Button VegRoll;
    @FXML
    private Button Rangoon;


    //bottom buttons
    @FXML
    private Button CancelButton; //match the fx:id value from Scene Builder
    
    @FXML
    private TextField AddNote; //match the fx:id value from Scene Builder
    
    @FXML
    private Button AddOrderButton; //match the fx:id value from Scene Builder
    
    private List<Integer> selectedItems = new ArrayList<>();;

    private int qApp;

    public void setSelectedItems(List<Integer> selectedItems) {
        this.selectedItems = selectedItems;
    }
    
    // This method runs automatically when the FXML loads
    @FXML
    public void initialize() {
        // Set up what happens when button is clicked
        ChickenRoll.setOnAction(e -> handleChickenRoll());
        VegRoll.setOnAction(e -> handleVegRoll());
        Rangoon.setOnAction(e -> handleRangoon());

        CancelButton.setOnAction(event -> switchScene("FXML/ServerOrder.fxml"));
        AddOrderButton.setOnAction(this::handleAddOrderButton);
    }
    
    // Your method to run the database query
    private void handleChickenRoll() {
        selectedItems.add(2100 + qApp);
    }

    private void handleVegRoll() {
        selectedItems.add(2000 + qApp);
    }

    private void handleRangoon() {
        selectedItems.add(1900 + qApp);
    }

    @FXML
    private void handleAddOrderButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ServerCheckout.fxml"));
            Parent root = loader.load();

            CheckoutController checkoutController = loader.getController();

            checkoutController.setSelectedItems(selectedItems);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchScene(String fileName){
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fileName));
            Stage stage = (Stage) CancelButton.getScene().getWindow();
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
        Stage stage = (Stage) CancelButton.getScene().getWindow();
        stage.close();
    }
}
