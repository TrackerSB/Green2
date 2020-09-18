package bayern.steinbrecher.green2.uninstaller.deleteConfigs;

import bayern.steinbrecher.wizard.WizardPageController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

import java.util.Optional;

public class DeleteConfigsController extends WizardPageController<Optional<Boolean>> {
    @FXML
    private CheckBox deleteConfigsCheckbox;

    @Override
    protected Optional<Boolean> calculateResult() {
        return Optional.of(deleteConfigsCheckbox.isSelected());
    }
}
