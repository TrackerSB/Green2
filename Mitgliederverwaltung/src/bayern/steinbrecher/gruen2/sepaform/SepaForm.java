/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.sepaform;

import bayern.steinbrecher.gruen2.WizardableView;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.people.Originator;
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
