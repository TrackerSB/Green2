/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.menu;

import bayern.steinbrecher.green2.View;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.membermanagement.MemberManagement;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents the main menu containing the main functions.
 *
 * @author Stefan Huber
 */
public class Menu extends View<MenuController> {

    private final MemberManagement caller;

    /**
     * Constructs a menu.
     *
     * @param caller The object for calling the functions represented by the menu.
     */
    public Menu(MemberManagement caller) {
        this.caller = caller;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        Parent root = loadFXML("Menu.fxml");
        root.getStylesheets().addAll(EnvironmentHandler.DEFAULT_STYLESHEET, "/menu.css");
        //TODO Think about moving this line to css file
        root.setStyle("-fx-padding: 0px");

        controller.setStage(stage);
        controller.setCaller(caller);

        stage.setScene(new Scene(root));
        stage.setTitle(EnvironmentHandler.getResourceValue("chooseProgram"));
        stage.setResizable(false);
        stage.getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());
        stage.show();
    }

    /**
     * Not implemented. There´s no data to be aborted by the user.
     *
     * @return Nothing.
     * @throws UnsupportedOperationException Always thrown.
     */
    @Override
    public boolean userAborted() {
        throw new UnsupportedOperationException("No data to abort");
    }
}