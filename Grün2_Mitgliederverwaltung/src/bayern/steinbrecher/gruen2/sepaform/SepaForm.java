/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2.sepaform;

import bayern.steinbrecher.gruen2.WizardableView;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.people.Originator;
import bayern.steinbrecher.wizard.WizardPage;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Represents a form for input according to sepa informations.
 *
 * @author Stefan Huber
 */
public class SepaForm
        extends WizardableView<Optional<Originator>, SepaFormController> {

    private Stage owner;

    /**
     * Default constructor. Owner set to {@code null}.
     */
    public SepaForm() {
        this(null);
    }

    /**
     * Creates a sepa form.
     *
     * @param owner The owner which causes this window to close if the owner is
     * closed. The owner is not blocked.
     */
    public SepaForm(Stage owner) {
        this.owner = owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        Parent root = loadFXML("SepaForm.fxml");
        controller.setStage(stage);

        stage.initOwner(owner);
        stage.setScene(new Scene(root));
        stage.setTitle(DataProvider.getResourceValue("sepaFormTitle"));
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.DEFAULT_ICON);
    }

    /**
     * Returns the originator currently represented. Returns
     * {@code Optional.empty} if the user did not confirm the input.
     *
     * @return The originator or {@code Optional.empty}.
     */
    public Optional<Originator> getOriginator() {
        return controller.getOriginator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WizardPage<Optional<Originator>> getWizardPage() {
        try {
            Pane root = loadFXML("SepaForm_Wizard.fxml");
            return new WizardPage<>(root, null, false, () -> getOriginator(),
                    controller.validProperty());
        } catch (IOException ex) {
            Logger.getLogger(SepaForm.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
