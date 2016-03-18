package bayern.steinbrecher.gruen2.sepaform;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.people.Originator;
import java.util.Optional;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents a form for input according to sepa informations.
 *
 * @author Stefan Huber
 */
public class SepaForm extends Application {

    private Stage primaryStage;
    private SepaFormController sfcontroller;
    private boolean wasShown = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource("SepaForm.fxml"));
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.getStylesheetPath());

        sfcontroller = fxmlLoader.getController();
        sfcontroller.setStage(primaryStage);

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Lastschrift Infos eintragen");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(DataProvider.getIcon());
    }

    public Optional<Originator> getOriginator() {
        if (primaryStage == null) {
            throw new IllegalStateException(
                    "start(...) has to be called first");
        }
        if (!wasShown) {
            primaryStage.showAndWait();
            wasShown = true;
        }
        return sfcontroller.getOriginator();
    }
}
