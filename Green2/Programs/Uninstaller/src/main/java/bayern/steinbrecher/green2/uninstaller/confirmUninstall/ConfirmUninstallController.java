package bayern.steinbrecher.green2.uninstaller.confirmUninstall;

import bayern.steinbrecher.wizard.WizardPageController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

import java.util.Optional;

public class ConfirmUninstallController extends WizardPageController<Optional<Void>> {
    @FXML
    private CheckBox confirmUninstall;

    @FXML
    private void initialize() {
        bindValidProperty(confirmUninstall.selectedProperty());
    }

    @Override
    protected Optional<Void> calculateResult() {
        return Optional.empty();
    }
}
