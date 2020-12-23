package bayern.steinbrecher.green2.launcher.progress;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.StandaloneWizardPage;

import java.util.Optional;

/**
 * Visualize progress of some task. The progress is in [0; 1].
 *
 * @author Stefan Huber
 * @since 2u14
 */
public class ProgressDialog extends StandaloneWizardPage<Optional<Void>, ProgressDialogController> {

    public ProgressDialog() {
        super("ProgressDialog.fxml", EnvironmentHandler.RESOURCE_BUNDLE);
        // FIXME Reapply config to stage
        // stage.setScene(new Scene(root));
        // stage.setResizable(false);
        // stage.setTitle(EnvironmentHandler.getResourceValue("downloadNewVersion"));
        // stage.initStyle(StageStyle.UTILITY);
    }

    public double getProgress() {
        return getController().getProgress();
    }

    public void setProgress(double progress) {
        getController().setProgress(progress);
    }
}
