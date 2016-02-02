package bayern.steinbrecher.gruen2.selection;

import bayern.steinbrecher.gruen2.data.DataProvider;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
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
public class Selection<T> extends Application {

    private Stage primaryStage;
    private SelectionController<T> scontroller;
    private final List<T> options;
    private boolean gotShown = false;
    private boolean gotClosed = false;

    public Selection(List<T> options) {
        this.options = options;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

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

    public List<T> getSelection() {
        onlyShowOnce();
        return scontroller.getSelection();
    }

    public double getContribution() {
        onlyShowOnce();
        return scontroller.getContribution();
    }

    private synchronized void onlyShowOnce() {
        if (!gotShown) {
            gotShown = true;
            primaryStage.showAndWait();
            gotClosed = true;
            notifyAll();
        }
        while (!gotClosed) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(Selection.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }
}
