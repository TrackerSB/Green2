/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
        root.getStylesheets().add(DataProvider.STYLESHEET_PATH);

        controller = fxmlLoader.getController();
        controller.setStage(stage);
        controller.setCaller(caller);

        stage.setScene(new Scene(root));
        stage.setTitle(DataProvider.getResourceValue("chooseProgram"));
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.DEFAULT_ICON);
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
