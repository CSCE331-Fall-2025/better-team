import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.stage.Stage;

import javafx.event.ActionEvent;

/**
 *  Controller for ServerDefault page
 */
public class ServerDefaultController {
    // URL & db saved for later use in connection to database
    private static final String URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/CSCE315Database";
    private dbSetup db = new dbSetup();

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
	
    //buttonbar, for adding new buttons.
	@FXML
	private ButtonBar EntreeBar;
    
    //list of items selected, to be sent to checkout
    private List<Integer> selectedItems = new ArrayList<>();;
    //quanity of the item to be picked
    private int qty = 1;
    
    //allows data to be passed between pages by setting qty of this
    public void setqty(int qty) {
        this.qty = qty;
    }   
    //allows data to be passed between pages by setting selected items of this
    public void setSelectedItems(List<Integer> selectedItems) {
        this.selectedItems = selectedItems;
    }

    /**
     * Runs upons page load, enables buttons and sets actions.
     */
    @FXML
    public void initialize() {
        // assign handlers to buttons
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
        //button buttoms
        CancelButton.setOnAction(event -> switchScene("/FXML/ServerOrder.fxml"));
        AddOrderButton.setOnAction(this::handleAddOrderButton);
        
        LoadNewButtons();
    }

    /**
     * Switches to checkout page with an order loaded, consisting of the 
     * SelectedItems list which is updated in checkout to match the list created here
     * @param event the event for which this is to be called, also used in finding source
     */
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

    //handlers for each button, technically overkill, but thorough

    //sides
    private void handleWhite_Steamed_Rice(){
        selectedItems.add(900 + qty);
    }

    private void handleFried_Rice() {
        selectedItems.add(300 + qty);
    }

    private void handleChow_Mein() {
        selectedItems.add(500 + qty);
    }

    private void handleSuper_Green() {
        selectedItems.add(1400 + qty);
    }

    //entree
    private void handleHoney_Walnut_Shrimp() {
        selectedItems.add(1000 + qty);
    }

    private void handleBeijing_Beef() {
        selectedItems.add(400 + qty);
    }

    private void handleBroccoli_Beef() {
        selectedItems.add(1300 + qty);
    }

    private void handleHoney_Sesame_Chicken() {
        selectedItems.add(2200 + qty);
    }

    private void handleKung_Pao_Chicken() {
        selectedItems.add(1100 + qty);
    }

    private void handleBlack_Pepper_Chicken() {
        selectedItems.add(1200 + qty);
    }

    private void handleOrange_Chicken() {
        selectedItems.add(100 + qty);
    }

    private void handleHot_Orange_Chicken() {
        selectedItems.add(700 + qty);
    }

    private void handleString_Bean_Chicken_Breast() {
        selectedItems.add(2300 + qty);
    }

    private void handleSuper_Green2() {
        selectedItems.add(1400 + qty);
    }
    /**
     * Adds a new button to EntreeBar, which on press runs handleAny on the given ID
     * @param text text to be displayed in the button
     * @param id id necessary for knowing which item to add to selectedItems
     */
    public void addNewButton(String text, int id) {/**/
        Button newButton = new Button(text);
        newButton.setOnAction(e->handleAny(id));
		ButtonBar.setButtonData(newButton, ButtonBar.ButtonData.LEFT);
        EntreeBar.getButtons().add(newButton);
    }
    /**
     * handler for any given button, based on the given id
     * Retroactively could've been used for others but made later out of necessity
     * @param x the passed in id
     */
    public void handleAny(int x){
        selectedItems.add(x*100 + qty);
    }
    /**
     *  Used once in Initialize to load buttons added postdev
     *  Checks the last ID in dishes, and for each id between the known last
     *  and that one, adds a button with the corresponding dish name and ID
     */
    private void LoadNewButtons(){
        int lastNormalDish = 46;//42
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

    /**
     * Switches to the next javafx scene.
     * @param fileName name of the .fxml file being switched too, path included if necessary.
     */
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
    //closes currently opened window
    private void closeWindow() { 
        Stage stage = (Stage) CancelButton.getScene().getWindow();
        stage.close();
    }
}
