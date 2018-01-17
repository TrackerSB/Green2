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
package bayern.steinbrecher.green2.contribution;

import bayern.steinbrecher.green2.ViewStartException;
import bayern.steinbrecher.green2.WizardableView;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.WizardPage;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Represents the contribution inserted.
 *
 * @author Stefan Huber
 */
public class Contribution extends WizardableView<Optional<Map<Color, Double>>, ContributionController> {

    /**
     * Default constructor. Represents a view for entering multiple values (in €) which are connected to a certain
     * color.
     */
    public Contribution() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startImpl(Stage stage) {
        Parent root;
        try {
            root = loadFXML("Contribution.fxml");
        } catch (IOException ex) {
            throw new ViewStartException(ex);
        }
        getController().setStage(stage);

        stage.setScene(new Scene(root));
        stage.setTitle(EnvironmentHandler.getResourceValue("contributionTitle"));
        stage.setResizable(false);
        stage.getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());
    }

    /**
     * Opens the contribution window if no other process yet opened one, blocks until the window is closed and returns
     * the value entered in the {@link TextField}. Returns {@link Optional#empty()} if the user did not confirm the
     * values. The window will only be opened ONCE; even if multiple threads are calling this function. They will be
     * blocked until the window is closed.
     *
     * @return The values and colors entered if any.
     * @see ContributionController#getContribution()
     */
    public Optional<Map<Color, Double>> getContribution() {
        showOnceAndWait();
        return getController().getContribution();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WizardPage<Optional<Map<Color, Double>>> getWizardPage() {
        try {
            Pane root = loadFXML("Contribution_Wizard.fxml");
            return new WizardPage<>(
                    root, null, false, () -> getController().getContribution(), getController().validProperty());
        } catch (IOException ex) {
            Logger.getLogger(Contribution.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
