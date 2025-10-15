package Database;
import javafx.beans.property.*;

public class Dish {
    private final StringProperty name;
    private final DoubleProperty price;

    public Dish(String name, double price) {
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
    }

    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
}
