package bayern.steinbrecher.gruen2.contribution;

import bayern.steinbrecher.gruen2.Model;
import bayern.steinbrecher.gruen2.data.DataProvider;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents the contribution inserted.
 *
 * @author Stefan Huber
 */
public class Contribution extends Model {

    private ContributionController ccontroller;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource("Contribution.fxml"));
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.getStylesheetPath());

        ccontroller = fxmlLoader.getController();
        ccontroller.setStage(primaryStage);

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Beitrag angeben");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(DataProvider.getIcon());
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
        return ccontroller.getContribution();
    }

    /**
     * Creates a contribution dialog, blocks until the user inserted a valid
     * number and returns the result.
     *
     * @return The inserted contribution.
     */
    public static Optional<Double> askForContribution() {
        try {
            Contribution c = new Contribution();
            c.start(new Stage());
            return c.getContribution();
        } catch (Exception ex) {
            Logger.getLogger(Contribution.class.getName())
                    .log(Level.SEVERE, null, ex);
            return Optional.empty();
        }
    }
}
