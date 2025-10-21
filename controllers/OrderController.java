import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code OrderController} class handles user interaction
 * on the order selection screen. It manages navigation between
 * menu categories (bowls, plates, drinks, etc.) and maintains
 * the shared list of selected items across different pages.
 */
public class OrderController {

    /** A static list that holds IDs of selected items across scenes. */
    private static final List<Integer> selectedItems = new ArrayList<>();

    @FXML private Button bowlButton;
    @FXML private Button plateButton;
    @FXML private Button bigPlateButton;
    @FXML private Button drinksButton;
    @FXML private Button appButton;
    @FXML private Button checkoutButton;
    @FXML private Button logoutButton;

    /**
     * Returns the list of selected item IDs shared across all pages.
     *
     * @return a list of selected item IDs
     */
    public static List<Integer> getSelectedItems() {
        return selectedItems;
    }

    /**
     * Initializes the controller after the FXML file is loaded.
     * This method sets up all the button actions with handlers.
     */
    @FXML
    public void initialize() {
        bowlButton.setOnAction(this::handleDefault);
        plateButton.setOnAction(this::handleDefault);
        bigPlateButton.setOnAction(this::handleDefault);

        appButton.setOnAction(this::handleApp);
        drinksButton.setOnAction(this::handleDrink);

        checkoutButton.setOnAction(this::handleCheckout);
        logoutButton.setOnAction(this::handlelogout);
    }

    /**
     * Handles clicks for default meal buttons
     * and loads the entree/side menu scene.
     *
     * @param event the click event from a button
     */
    @FXML
    private void handleDefault(ActionEvent event) {
        switchSceneWithList(event, "/FXML/ServerDefault.fxml");
    }

    /**
     * Handles clicks on the appetizer button.
     *
     * @param event the click event from a button
     */
    @FXML
    private void handleApp(ActionEvent event) {
        switchSceneWithList(event, "/FXML/ServerApp.fxml");
    }

    /**
     * Handles clicks on the drink button.
     *
     * @param event the click event from a button
     */
    @FXML
    private void handleDrink(ActionEvent event) {
        switchSceneWithList(event, "/FXML/ServerDrink.fxml");
    }

    /**
     * Handles clicks on the checkout button.
     *
     * @param event the click event from a button
     */
    @FXML
    private void handleCheckout(ActionEvent event) {
        switchSceneWithList(event, "/FXML/ServerCheckout.fxml");
    }

    /**
     * Handles clicks on the logout button
     * and returns to the login scene.
     *
     * @param event the click event from a button
     */
    @FXML
    private void handlelogout(ActionEvent event) {
        switchSceneWithList(event, "/FXML/Login.fxml");
    }

    /**
     * Switches scenes while preserving the selected items.
     * This ensures that the cart is kept between pages.
     *
     * @param event the button click event that triggered the scene change
     * @param fxmlFile the relative path to the FXML file for the new scene
     */
    private void switchSceneWithList(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Object ctrl = loader.getController();
            if (ctrl instanceof ServerDefaultController) {
                ((ServerDefaultController) ctrl).setSelectedItems(selectedItems);
            } else if (ctrl instanceof ServerAppController) {
                ((ServerAppController) ctrl).setSelectedItems(selectedItems);
            } else if (ctrl instanceof ServerDrinkController) {
                ((ServerDrinkController) ctrl).setSelectedItems(selectedItems);
            } else if (ctrl instanceof CheckoutController) {
                ((CheckoutController) ctrl).setSelectedItems(selectedItems);
            }

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
