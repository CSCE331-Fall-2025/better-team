import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;


import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;

import javafx.stage.Stage;

import javafx.event.ActionEvent;

import java.io.IOException;

public class ServerDefaultController {

    private static final String URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/CSCE315Database";
    private dbSetup db = new dbSetup();
    /*
    public void setSelectedItems(List<Integer> selectedItems) {
        this.selectedItems = selectedItems;
    }*/

    //sides
    @FXML
    private Button White_Steamed_Rice;
    @FXML
    private Button Fried_Rice;
    @FXML
    private Button Chow_Mein;
    @FXML
    private Button Super_Green;

    //entrees
    @FXML
    private Button Honey_Walnut_Shrimp;
    @FXML
    private Button Beijing_Beef;
    @FXML
    private Button Broccoli_Beef;
    @FXML
    private Button Honey_Sesame_Chicken;
    @FXML
    private Button Kung_Pao_Chicken;
    @FXML
    private Button Black_Pepper_Chicken;
    @FXML
    private Button Orange_Chicken;
    @FXML
    private Button Hot_Orange_Chicken;
    @FXML
    private Button String_Bean_Chicken_Breast;
    @FXML
    private Button Super_Green2;


    //bottom buttons
    @FXML
    private Button CancelButton; //match the fx:id value from Scene Builder
    
    @FXML
    private TextField AddNote; //match the fx:id value from Scene Builder
    
    @FXML
    private Button AddOrderButton; //match the fx:id value from Scene Builder
    
    @FXML
    private ButtonBar EntreeBar;

    //stuff
    
    private List<Integer> selectedItems = new ArrayList<>();;
    private int qEntree; 
    private int qSide; 
    private int qApp;
    
    public void setSelectedItems(List<Integer> selectedItems) {
        this.selectedItems = selectedItems;
    }

    @FXML
    public void initialize() {
        /*loadButtonsFromFile();*/
        //sides
        White_Steamed_Rice.setOnAction(event -> handleWhite_Steamed_Rice());
        Fried_Rice.setOnAction(event -> handleFried_Rice());
        Chow_Mein.setOnAction(event -> handleChow_Mein());
        Super_Green.setOnAction(event -> handleSuper_Green());

        //entrees
        Honey_Walnut_Shrimp.setOnAction(event -> handleHoney_Walnut_Shrimp());
        Beijing_Beef.setOnAction(event -> handleBeijing_Beef());
        Broccoli_Beef.setOnAction(event -> handleBroccoli_Beef());
        Honey_Sesame_Chicken.setOnAction(event -> handleHoney_Sesame_Chicken());
        Kung_Pao_Chicken.setOnAction(event -> handleKung_Pao_Chicken());
        Black_Pepper_Chicken.setOnAction(event -> handleBlack_Pepper_Chicken());
        Orange_Chicken.setOnAction(event -> handleOrange_Chicken());
        Hot_Orange_Chicken.setOnAction(event -> handleHot_Orange_Chicken());
        String_Bean_Chicken_Breast.setOnAction(event -> handleString_Bean_Chicken_Breast());
        Super_Green2.setOnAction(event -> handleSuper_Green2());

        CancelButton.setOnAction(event -> switchScene("/FXML/ServerOrder.fxml"));
        AddOrderButton.setOnAction(this::handleAddOrderButton);

        LoadNewButtons();
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

    private void handleWhite_Steamed_Rice(){
        selectedItems.add(900 + qSide);
    }

    private void handleFried_Rice() {
        selectedItems.add(300 + qSide);
    }

    private void handleChow_Mein() {
        selectedItems.add(500 + qSide);
    }

    private void handleSuper_Green() {
        selectedItems.add(1400 + qSide);
    }

    //entree
    private void handleHoney_Walnut_Shrimp() {
        selectedItems.add(1000 + qEntree);
    }

    private void handleBeijing_Beef() {
        selectedItems.add(400 + qEntree);
    }

    private void handleBroccoli_Beef() {
        selectedItems.add(1300 + qEntree);
    }

    private void handleHoney_Sesame_Chicken() {
        selectedItems.add(2200 + qEntree);
    }

    private void handleKung_Pao_Chicken() {
        selectedItems.add(1100 + qEntree);
    }

    private void handleBlack_Pepper_Chicken() {
        selectedItems.add(1200 + qEntree);
    }

    private void handleOrange_Chicken() {
        selectedItems.add(100 + qEntree);
    }

    private void handleHot_Orange_Chicken() {
        selectedItems.add(700 + qEntree);
    }

    private void handleString_Bean_Chicken_Breast() {
        selectedItems.add(2300 + qEntree);
    }

    private void handleSuper_Green2() {
        selectedItems.add(1400 + qEntree);
        addNewButton("test33", 15);
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

    public void addNewButton(String text, int id) {
        Button newButton = new Button(text);
        newButton.setOnAction(e->handleAny(id));
        ButtonBar.setButtonData(newButton, ButtonData.LEFT);
        EntreeBar.getButtons().add(newButton);

        /*savedButtons.add(new ButtonInfo(text, id));
        saveButtonsToFile();*/
    }
    
    public void handleAny(int x){
        selectedItems.add(x*100 + qEntree);
    }

    private void LoadNewButtons(){
        int lastNormalDish = 44;//42
        int lastDish = 41;//use connection to get last row
        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(URL, my.user, my.pswd);

            String sql = "SELECT dish_id FROM dish ORDER BY dish_id DESC LIMIT 1";
            PreparedStatement ps1 = conn.prepareStatement(sql);
            ResultSet rs1 = ps1.executeQuery();
            rs1.next(); lastDish = rs1.getInt(1);
            ps1.close();

            for (int k = lastNormalDish; k<=lastDish; k++){//k=id of dish
                String name = "ex";//use connection to get name current row
                
                String sql2 = "select name from dish where dish_id = " + k ;
                PreparedStatement ps = conn.prepareStatement(sql2);
                ResultSet rs = ps.executeQuery();
                rs.next(); name = rs.getString(1);
                ps.close();

                addNewButton(name, k);
            }

            conn.close();//end

        } catch (Exception e) {e.printStackTrace();}


    }

    /* save/load button */
    /*
    List<ButtonInfo> savedButtons = new ArrayList<>();

    public class ButtonInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        String text; int id;
        public ButtonInfo(String text, int id){
            this.text = text; this.id = id;
        }
    }

    private void saveButtonsToFile() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("buttons.dat"))) {
            out.writeObject(savedButtons);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadButtonsFromFile() {
    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("buttons.dat"))) {
        savedButtons = (List<ButtonInfo>) in.readObject();
        for (ButtonInfo info : savedButtons) {
            addNewButton(info.text, info.id);
        }
    } catch (Exception e) {
        savedButtons = new ArrayList<>();
    }
}*/

    // also setup an onclick action to change selected items
    //using handleAny?
}
