package bayern.steinbrecher.gruen2.main;

import bayern.steinbrecher.gruen2.data.DataProvider;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Der Startpunkt für die komplette Applikation.
 *
 * @author Stefan Huber
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader
                = new FXMLLoader(getClass().getResource("MainView.fxml"));

        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.getStylesheetPath());
        ((MainController) fxmlLoader.getController()).setStage(primaryStage);

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Programm wählen");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(DataProvider.getIcon());
        primaryStage.show();
    }
}
