
import java.sql.*;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import javafx.collections.ObservableList;

import java.util.List;
import java.util.ArrayList;

public class checkoutController {

    @FXML
    private TextArea text; //match the fx:id value from Scene Builder

    private List<Integer> selectedItems;
    
    
    // This method runs automatically when the FXML loads
    @FXML
    public void initialize() {

    }
    public void setSelectedItems(List<Integer> selectedItems) {
        this.selectedItems = selectedItems;
        text.setText(selectedItems.toString());
    }
}