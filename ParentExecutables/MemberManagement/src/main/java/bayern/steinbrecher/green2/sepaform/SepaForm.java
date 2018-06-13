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
package bayern.steinbrecher.green2.sepaform;

import bayern.steinbrecher.green2.ViewStartException;
import bayern.steinbrecher.green2.WizardableView;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.people.Originator;
import java.io.IOException;
import java.util.Optional;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents a form for input according to sepa informations.
 *
 * @author Stefan Huber
 */
public class SepaForm extends WizardableView<Optional<Originator>, SepaFormController> {

    /**
     * Default constructor. Represents a view for entering data needed by SEPA Direct Debits.
     */
    public SepaForm() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startImpl(Stage stage) {
        Parent root;
        try {
            root = loadFXML("SepaFormParent.fxml");
        } catch (IOException ex) {
            throw new ViewStartException(ex);
        }
        getController().setStage(stage);

        stage.setScene(new Scene(root));
        stage.setTitle(EnvironmentHandler.getResourceValue("sepaFormTitle"));
        stage.setResizable(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getWizardFxmlPath() {
        return "SepaFormWizard.fxml";
    }
}
