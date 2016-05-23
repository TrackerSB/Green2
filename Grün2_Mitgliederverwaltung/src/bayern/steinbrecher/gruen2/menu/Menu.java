package bayern.steinbrecher.gruen2.menu;

import bayern.steinbrecher.gruen2.View;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.main.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents the main menu containing the main functions.
 *
 * @author Stefan Huber
 */
public class Menu extends View {

    private MenuController mcontroller;
    private Main caller;

    public Menu(Main caller) {
        this.caller = caller;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        FXMLLoader fxmlLoader
                = new FXMLLoader(getClass().getResource("Menu.fxml"));
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.getStylesheetPath());

        mcontroller = fxmlLoader.getController();
        mcontroller.setStage(stage);
        mcontroller.setCaller(caller);

        stage.setScene(new Scene(root));
        stage.setTitle("Programm wählen");
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.getIcon());
        stage.show();
    }
}
