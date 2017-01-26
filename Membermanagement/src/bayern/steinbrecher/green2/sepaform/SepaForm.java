/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */
package bayern.steinbrecher.green2.sepaform;

import bayern.steinbrecher.green2.WizardableView;
import bayern.steinbrecher.green2.data.DataProvider;
import bayern.steinbrecher.green2.people.Originator;
import bayern.steinbrecher.wizard.WizardPage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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
     *              closed. The owner is not blocked.
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
        stage.getIcons().add(DataProvider.ImageSet.LOGO.get());
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
            return new WizardPage<>(root, null, false, this::getOriginator, controller.validProperty());
        } catch (IOException ex) {
            Logger.getLogger(SepaForm.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
