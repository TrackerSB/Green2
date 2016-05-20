package bayern.steinbrecher.gruen2.menu;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.main.Main;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents the main menu containing the main functions.
 *
 * @author Stefan Huber
 */
public class Menu extends Application {

    private MenuController mcontroller;
    private Stage primaryStage;
    private Main main;

    public Menu(Main main) {
        this.main = main;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        FXMLLoader fxmlLoader
                = new FXMLLoader(getClass().getResource("Menu.fxml"));
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.getStylesheetPath());

        mcontroller = fxmlLoader.getController();
        mcontroller.setStage(primaryStage);
        mcontroller.setMain(main);

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Programm wählen");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(DataProvider.getIcon());
        primaryStage.show();
    }
}
