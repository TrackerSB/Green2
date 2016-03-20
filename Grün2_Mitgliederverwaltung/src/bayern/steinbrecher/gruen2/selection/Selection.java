package bayern.steinbrecher.gruen2.selection;

import bayern.steinbrecher.gruen2.Model;
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
public class Selection<T extends Comparable> extends Model {

    private SelectionController<T> scontroller;
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
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource("Selection.fxml"));
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.getStylesheetPath());

        scontroller = fxmlLoader.getController();
        scontroller.setStage(primaryStage);
        scontroller.setOptions(options);

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Auswählen");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(DataProvider.getIcon());
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
        if (stage == null) {
            throw new IllegalStateException(
                    "start(...) has to be called first");
        }
        onlyShowOnce();
        return scontroller.getSelection();
    }

    /**
     * Opens the selection window if no other process yet opened one, blocks
     * until the window is closed and returns the value entered in the
     * {@code TextField}. Returns {@code Optional.empty} if the user did not
     * confirm the contribution. The window will only be opened ONCE; even if
     * multiple threads are calling this function. They will be blocked until
     * the window is closed.
     *
     * @return The value entered in the the {@code TextField} if any.
     */
    public Optional<Double> getContribution() {
        if (stage == null) {
            throw new IllegalStateException(
                    "start(...) has to be called first");
        }
        onlyShowOnce();
        return scontroller.getContribution();
    }
}
