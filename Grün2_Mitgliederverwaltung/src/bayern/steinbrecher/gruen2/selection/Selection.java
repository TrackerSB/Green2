package bayern.steinbrecher.gruen2.selection;

import bayern.steinbrecher.gruen2.View;
import bayern.steinbrecher.gruen2.data.DataProvider;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents a selection dialog.
 *
 * @author Stefan Huber
 * @param <T> The type of the attributes being able to selct.
 */
public class Selection<T extends Comparable>
        extends View<SelectionController<T>> {

    private final List<T> options;

    /**
     * Greates a new Frame representing the given options as selectable
     * {@code CheckBox}es and representing a {@code TextField} for entering a
     * number.
     *
     * @param options The options the user is allowed to select.
     */
    public Selection(List<T> options) {
        this.options = options;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource("Selection.fxml"));
        fxmlLoader.setResources(DataProvider.RESOURCE_BUNDLE);
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.getStylesheetPath());

        controller = fxmlLoader.getController();
        controller.setStage(stage);
        controller.setOptions(options);

        stage.setScene(new Scene(root));
        stage.setTitle(DataProvider.RESOURCE_BUNDLE.getString("select"));
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.getIcon());
    }

    /**
     * Opens the selection window if no other process yet opened one, blocks
     * until the window is closed and retruns the list of items which were
     * selected. Returns {@code Optional.empty} if the user did not confirm the
     * selection. The window will only be opened ONCE; even if multiple threads
     * are calling this function. They will be blocked until the window is
     * closed.
     *
     * @return The selection if any.
     */
    public Optional<List<T>> getSelection() {
        showOnceAndWait();
        return controller.getSelection();
    }
}
