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

public class OrderController {


    private static final List<Integer> selectedItems = new ArrayList<>();

    @FXML private Button bowlButton;
    @FXML private Button plateButton;
    @FXML private Button bigPlateButton;
    @FXML private Button drinksButton;
    @FXML private Button appButton;
    @FXML private Button checkoutButton;
    @FXML private Button logoutButton;

    public static List<Integer> getSelectedItems() {
        return selectedItems;
    }

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

    @FXML
    private void handleDefault(ActionEvent event) {
        switchSceneWithList(event, "ServerDefault.fxml");
    }

    @FXML
    private void handleApp(ActionEvent event) {
        switchSceneWithList(event, "ServerApp.fxml");
    }

    @FXML
    private void handleDrink(ActionEvent event) {
        switchSceneWithList(event, "ServerDrink.fxml");
    }

    @FXML
    private void handleCheckout(ActionEvent event) {
        switchSceneWithList(event, "ServerCheckout.fxml");
    }

    @FXML
    private void handlelogout(ActionEvent event) {
        switchSceneWithList(event, "Login.fxml");
    }

    /**
     * Moves the shopping cart across pages.
     * The fxmlfile argument must be a valid fxmlfile.
     * This function just works when you pass an event and file in.
     * The screen will load with the new scene but keep the shopping cart.
     * @param event this is for any event, usually a click with a button.
     * @param fxmlFile the file name of the new fxml page.
     */
    private void switchSceneWithList(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Object ctrl = loader.getController();
            if (ctrl instanceof serverDefaultController) {
                serverDefaultController defaultCtrl = (serverDefaultController) ctrl;
                defaultCtrl.setSelectedItems(selectedItems);
            } else if (ctrl instanceof serverAppController) {
                serverAppController appCtrl = (serverAppController) ctrl;
                appCtrl.setSelectedItems(selectedItems);
            } else if (ctrl instanceof serverDrinkController) {
                serverDrinkController drinkCtrl = (serverDrinkController) ctrl;
                drinkCtrl.setSelectedItems(selectedItems);
            } else if (ctrl instanceof checkoutController) {
                checkoutController checkoutCtrl = (checkoutController) ctrl;
                checkoutCtrl.setSelectedItems(selectedItems);
            }

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
