package bayern.steinbrecher.gruen2.launcher;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.Initializable;

/**
 * Controller of download dialog.
 *
 * @author Stefan Huber
 */
public class Grün2LauncherController implements Initializable {

    private static final DecimalFormat FORMAT = new DecimalFormat("#.0");
    private final DoubleProperty percentage = new SimpleDoubleProperty();
    private final StringProperty percentageString = new SimpleStringProperty();

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        percentage.addListener((obs, oldVal, newVal) -> {
            percentageString.set(FORMAT.format(newVal.doubleValue() * 100));
        });
    }

    public ReadOnlyDoubleProperty percentageProperty() {
        return percentage;
    }

    public double getPercentage() {
        return percentage.get();
    }

    public void incPercentage() {
        if (percentage.get() < 1) {
            percentage.set(percentage.get() + 0.001);
        }
    }

    public ReadOnlyStringProperty percentageStringProperty() {
        return percentageString;
    }

    public String getPercentageString() {
        return percentageString.get();
    }
}
