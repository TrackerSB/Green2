package bayern.steinbrecher.green2.launcher.progress;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.WizardPage;

import java.util.Optional;

/**
 * A dialog showing progress of any operation.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public class ProgressDialog extends WizardPage<Optional<Void>, ProgressDialogController> {

    public ProgressDialog() {
        super("ProgressDialog.fxml", EnvironmentHandler.RESOURCE_BUNDLE);
        // FIXME Reapply config to stage
        // stage.setScene(new Scene(root));
        // stage.setResizable(false);
        // stage.setTitle(EnvironmentHandler.getResourceValue("downloadNewVersion"));
        // stage.initStyle(StageStyle.UTILITY);
    }

    /**
     * Returns the progress in %.
     *
     * @return The progress in %..
     * @see ProgressDialogController#percentageProperty()
     */
    public double getPercentage() {
        return getController().getPercentage();
    }

    /**
     * Increases the value of the progress by 1.0/{@code steps}.
     *
     * @param steps The count of steps 100% is split.
     * @see ProgressDialogController#percentageProperty()
     */
    public void incPercentage(int steps) {
        getController().incPercentage(steps);
    }
}
