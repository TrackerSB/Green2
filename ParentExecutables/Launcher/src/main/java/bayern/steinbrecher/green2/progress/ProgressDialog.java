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
package bayern.steinbrecher.green2.progress;

import bayern.steinbrecher.green2.View;
import bayern.steinbrecher.green2.ViewStartException;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A dialog showing progress of any operation.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public class ProgressDialog extends View<ProgressDialogController> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startImpl(Stage stage) {
        Parent root;
        try {
            root = loadFXML("ProgressDialog.fxml");
        } catch (IOException ex) {
            throw new ViewStartException(ex);
        }
        getController().setStage(stage);

        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setTitle(EnvironmentHandler.getResourceValue("downloadNewVersion"));
        stage.initStyle(StageStyle.UTILITY);
    }

    /**
     * Returns the value hold by {@code percentageProperty}.
     *
     * @return The value hold by {@code percentageProperty}.
     * @see #percentageProperty()
     */
    public double getPercentage() {
        return getController().getPercentage();
    }

    /**
     * Increases the value of {@link ProgressDialogController#percentageProperty()} by 1.0/{@code steps}.
     *
     * @param steps The count of steps 100% is split.
     */
    public void incPercentage(int steps) {
        getController().incPercentage(steps);
    }
}
