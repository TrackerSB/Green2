/*
 * Copyright (C) 2018 Stefan Huber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.menu;

import bayern.steinbrecher.green2.View;
import bayern.steinbrecher.green2.ViewStartException;
import bayern.steinbrecher.green2.connection.DBConnection;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents the main menu containing the main functions.
 *
 * @author Stefan Huber
 */
public class MainMenu extends View<MainMenuController> {

    private transient final DBConnection dbConnection;

    /**
     * Creates a Menu which contains controls for all the functionality to be used by the user.
     *
     * @param dbConnection The connection to use for querying data.
     */
    public MainMenu(DBConnection dbConnection) {
        super();
        this.dbConnection = dbConnection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void callWhenLoadFXML() {
        getController().setDbConnection(dbConnection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startImpl(Stage stage) {
        Parent root;
        try {
            root = loadFXML("MainMenu.fxml");
        } catch (IOException ex) {
            throw new ViewStartException(ex);
        }

        getController().setStage(stage);

        stage.setScene(new Scene(root));
        stage.setTitle(EnvironmentHandler.getResourceValue("chooseProgram"));
        stage.setResizable(false);
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
