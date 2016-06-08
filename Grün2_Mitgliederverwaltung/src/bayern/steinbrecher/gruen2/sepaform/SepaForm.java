package bayern.steinbrecher.gruen2.sepaform;

import bayern.steinbrecher.gruen2.View;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.people.Originator;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents a form for input according to sepa informations.
 *
 * @author Stefan Huber
 */
public class SepaForm extends View<SepaFormController> {

    private Stage owner;

    /**
     * Default constructor. Owner set to {@code null}.
     */
    public SepaForm() {
        this(null);
    }

    /**
     * Creates a sepa form.
     *
     * @param owner The owner which causes this window to close if the owner is
     * closed. The owner is not blocked.
     */
    public SepaForm(Stage owner) {
        this.owner = owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource("SepaForm.fxml"));
        fxmlLoader.setResources(DataProvider.RESOURCE_BUNDLE);
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.getStylesheetPath());

        controller = fxmlLoader.getController();
        controller.setStage(stage);

        stage.initOwner(owner);
        stage.setScene(new Scene(root));
        stage.setTitle(DataProvider.RESOURCE_BUNDLE.getString("sepaFormTitle"));
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.getIcon());
    }

    /**
     * Opens the sepa form window if no other process yet opened one, blocks
     * until the window is closed and returns the originator. Returns
     * {@code Optional.empty} if the user did not confirm the input. The window
     * will only be opened ONCE; even if multiple threads are calling this
     * function. They will be blocked until the window is closed.
     *
     * @return The originator or {@code Optional.empty}.
     */
    public Optional<Originator> getOriginator() {
        showOnceAndWait();
        return controller.getOriginator();
    }
}
