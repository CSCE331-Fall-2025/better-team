import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Item {

    private final StringProperty name;
    private final IntegerProperty current;
    private final IntegerProperty recommended;
    private final IntegerProperty difference;

    public Item(String name, int current, int recommended) {
        this.name = new SimpleStringProperty(name);
        this.current = new SimpleIntegerProperty(current);
        this.recommended = new SimpleIntegerProperty(recommended);
        this.difference = new SimpleIntegerProperty(recommended - current);
    }

    public StringProperty nameProperty() { return name; }
    public IntegerProperty currentProperty() { return current; }
    public IntegerProperty recommendedProperty() { return recommended; }
    public IntegerProperty differenceProperty() { return difference; }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    public int getCurrent() { return current.get(); }
    public void setCurrent(int current) { this.current.set(current); difference.set(recommended.get() - current); }

    public int getRecommended() { return recommended.get(); }
    public void setRecommended(int recommended) { this.recommended.set(recommended); difference.set(recommended - current.get()); }

    public int getDifference() { return difference.get(); }
}
