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
public class Menu extends View<MenuController> {

    private Main caller;

    /**
     * Constructes a menu.
     *
     * @param caller The object for calling the functions represented by the
     * menu.
     */
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
        fxmlLoader.setResources(DataProvider.RESOURCE_BUNDLE);
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.getStylesheetPath());

        controller = fxmlLoader.getController();
        controller.setStage(stage);
        controller.setCaller(caller);

        stage.setScene(new Scene(root));
        stage.setTitle(DataProvider.RESOURCE_BUNDLE.getString("chooseProgram"));
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.getIcon());
        stage.show();
    }

    /**
     * Not implemented. There´s no data to be confirmed by the user.
     *
     * @return Nothing.
     * @throws UnsupportedOperationException Always thrown.
     */
    @Override
    public boolean userConfirmed() {
        throw new UnsupportedOperationException("No data to confirm");
    }
}
