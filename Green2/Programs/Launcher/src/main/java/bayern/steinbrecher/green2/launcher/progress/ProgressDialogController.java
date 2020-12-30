package bayern.steinbrecher.green2.launcher.progress;

import bayern.steinbrecher.wizard.StandaloneWizardPageController;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;

import java.text.DecimalFormat;
import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 2u14
 */
public class ProgressDialogController extends StandaloneWizardPageController<Optional<Void>> {

    private static final DecimalFormat FORMAT = new DecimalFormat("#0.0");
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, "percentage", 0);
    private final ReadOnlyStringWrapper progressString = new ReadOnlyStringWrapper(this, "percentageStrnig", "0%");

    @FXML
    public void initialize() {
        progress.addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                String formattedPercentValue;
                //CHECKSTYLE.OFF: MagicNumber - Multiplication with 100 is needed for having output as percentage.
                synchronized (FORMAT) {
                    formattedPercentValue = FORMAT.format(Math.max(0, Math.min(newVal.doubleValue(), 1)) * 100);
                }
                //CHECKSTYLE.ON: MagicNumber
                progressString.set(formattedPercentValue + "% ");
            });
        });
    }

    public ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }

    public double getProgress() {
        return progress.get();
    }

    public void setProgress(double progress) {
        if (progress < 0 || progress > 1) {
            throw new IllegalArgumentException("Progress has to be within [0; 1]");
        }
        this.progress.set(progress);
    }

    public ReadOnlyStringProperty progressStringProperty() {
        return progressString.getReadOnlyProperty();
    }

    public String getProgressString() {
        return progressString.get();
    }

    @Override
    protected Optional<Void> calculateResult() {
        return Optional.empty();
    }
}
