package bayern.steinbrecher.green2.programs.launcher.progress;

import bayern.steinbrecher.wizard.WizardPageController;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;

import java.text.DecimalFormat;
import java.util.Optional;

/**
 * The controller of a {@link ProgressDialog}.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public class ProgressDialogController extends WizardPageController<Optional<Void>> {

    private static final DecimalFormat FORMAT = new DecimalFormat("#0.0");
    private final ReadOnlyDoubleWrapper percentage = new ReadOnlyDoubleWrapper(this, "percentage", 0);
    private final ReadOnlyStringWrapper percentageString = new ReadOnlyStringWrapper(this, "percentageStrnig", "0%");

    @FXML
    public void initialize() {
        percentage.addListener((obs, oldVal, newVal) -> {
            //CHECKSTYLE.OFF: MagicNumber - Multiplication with 100 is needed for having output as percentage.
            percentageString.set(FORMAT.format(Math.min(newVal.doubleValue(), 1) * 100) + "% ");
            //CHECKSTYLE.ON: MagicNumber
        });
    }

    /**
     * Returns the property containing the current value representing the progress of download.
     *
     * @return The property containing the current value representing the progress of download.
     */
    public ReadOnlyDoubleProperty percentageProperty() {
        return percentage.getReadOnlyProperty();
    }

    /**
     * Returns the value hold by {@code percentageProperty}.
     *
     * @return The value hold by {@code percentageProperty}.
     * @see #percentageProperty()
     */
    public double getPercentage() {
        return percentage.get();
    }

    /**
     * Increases the value of {@code percentageProperty()} by 1.0/{@code steps}.
     *
     * @param steps The count of steps 100% is split.
     */
    public void incPercentage(int steps) {
        if (percentage.get() < 1) { //NOPMD - 1 represents 100%.
            percentage.set(percentage.get() + 1.0 / steps);
        }
    }

    /**
     * Returns the property which contains a {@link String} representation of the value hold by
     * {@code percentageProperty}.
     *
     * @return The property which contains a {@link String} representation of the value hold by
     * {@code percentageProperty}.
     * @see #percentageProperty()
     */
    public ReadOnlyStringProperty percentageStringProperty() {
        return percentageString.getReadOnlyProperty();
    }

    /**
     * Returns the value hold by {@code percentageStringProperty()}.
     *
     * @return The value hold by {@code percentageStringProperty()}.
     * @see #percentageStringProperty()
     */
    public String getPercentageString() {
        return percentageString.get();
    }

    @Override
    protected Optional<Void> calculateResult() {
        return Optional.empty();
    }
}
