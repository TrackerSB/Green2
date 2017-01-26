/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.menu;

import bayern.steinbrecher.green2.View;
import bayern.steinbrecher.green2.data.DataProvider;
import bayern.steinbrecher.green2.main.Main;
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
     *               menu.
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

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Menu.fxml"));
        fxmlLoader.setResources(DataProvider.RESOURCE_BUNDLE);
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.STYLESHEET_PATH);

        controller = fxmlLoader.getController();
        controller.setStage(stage);
        controller.setCaller(caller);

        stage.setScene(new Scene(root));
        stage.setTitle(DataProvider.getResourceValue("chooseProgram"));
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.ImageSet.LOGO.get());
        stage.show();
    }

    /**
     * Not implemented. There´s no data to be abborted by the user.
     *
     * @return Nothing.
     * @throws UnsupportedOperationException Always thrown.
     */
    @Override
    public boolean userAbborted() {
        throw new UnsupportedOperationException("No data to abbort");
    }
}
