package bayern.steinbrecher.gruen2.selection;

import bayern.steinbrecher.gruen2.data.DataProvider;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents a selection dialog.
 *
 * @author Stefan Huber
 */
public class Selection<T> extends Application {

    private Stage primaryStage;
    private SelectionController<T> scontroller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource("Selection.fxml"));
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.getStylesheetPath());

        scontroller = fxmlLoader.getController();
        scontroller.setStage(primaryStage);

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Auswählen");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(DataProvider.getIcon());
    }
    
    public void setOptions(List<T> options){
        scontroller.setOptions(options);
    }

    public List<T> getSelection() {
        primaryStage.showAndWait();
        return scontroller.getSelection();
    }
}
