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

public class serverDrinkController {

    public void setSelectedItems(List<Integer> selectedItems) {
        this.selectedItems = selectedItems;
    }

    //drinks
    @FXML
    private Button Coke;
    @FXML
    private Button Sprite;
    @FXML
    private Button DrPepper;
    @FXML
    private Button SweetTea;

    @FXML
    private Button MangoGuava;
    @FXML
    private Button PomPin;
    @FXML
    private Button WatMan;
    @FXML
    private Button PeachLychee;


    //bottom buttons
    @FXML
    private Button CancelButton; //match the fx:id value from Scene Builder
    
    @FXML
    private TextField AddNote; //match the fx:id value from Scene Builder
    
    @FXML
    private Button AddOrderButton; //match the fx:id value from Scene Builder
    
    private List<Integer> selectedItems = new ArrayList<>();;
    
    // This method runs automatically when the FXML loads
    @FXML
    public void initialize() {
        // Set up what happens when button is clicked
        Coke.setOnAction(e -> handleCoke());
        Sprite.setOnAction(e -> handleSprite());
        DrPepper.setOnAction(e -> handleDrPepper());
        SweetTea.setOnAction(e -> handleSweetTea());
        MangoGuava.setOnAction(e -> handleMangoGuava());
        PomPin.setOnAction(e -> handlePomPin());
        WatMan.setOnAction(e -> handleWatMan());
        PeachLychee.setOnAction(e -> handlePeachLychee());

        CancelButton.setOnAction(event -> switchScene("serverOrder.fxml"));
        AddOrderButton.setOnAction(this::handleAddOrderButton);
    }
    
    // Your method to run the database query
    private void handleCoke() {
        selectedItems.add(601);
    }

    private void handleSprite() {
        selectedItems.add(201);
    }

    private void handleDrPepper() {
        selectedItems.add(801);
    }

    private void handleSweetTea() {
        selectedItems.add(2501);
    }

    private void handleMangoGuava() {
        selectedItems.add(1501);
    }

    private void handlePomPin() {
        selectedItems.add(1601);
    }

    private void handleWatMan() {
        selectedItems.add(1701);
    }

    private void handlePeachLychee() {
        selectedItems.add(1801);
    }

    @FXML
    private void handleAddOrderButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("serverCheckOut.fxml"));
            Parent root = loader.load();

            checkoutController checkoutController = loader.getController();

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