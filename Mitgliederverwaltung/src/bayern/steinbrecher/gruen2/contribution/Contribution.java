/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.contribution;

import bayern.steinbrecher.gruen2.WizardableView;
import bayern.steinbrecher.gruen2.data.DataProvider;
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
 * Represents the contribution inserted.
 *
 * @author Stefan Huber
 */
public class Contribution
        extends WizardableView<Optional<Double>, ContributionController> {

    private Stage owner;

    /**
     * Default constructor. Owner set to {@code null}.
     */
    public Contribution() {
        this(null);
    }

    /**
     * Creates a contribution dialog.
     *
     * @param owner The owner which causes this window to close if the owner is
     * closed. The owner is not blocked.
     */
    public Contribution(Stage owner) {
        this.owner = owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        Parent root = loadFXML("Contribution.fxml");
        controller.setStage(stage);

        stage.initOwner(owner);
        stage.setScene(new Scene(root));
        stage.setTitle(DataProvider.getResourceValue("contributionTitle"));
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.DEFAULT_ICON);
    }

    /**
     * Opens the contribution window if no other process yet opened one, blocks
     * until the window is closed and returns the value entered in the
     * {@code TextField}. Returns {@code Optional.empty} if the user did not
     * confirm the contribution. The window will only be opened ONCE; even if
     * multiple threads are calling this function. They will be blocked until
     * the window is closed.
     *
     * @return The value entered in the the {@code TextField} if any.
     * @see ContributionController#getContribution()
     */
    public Optional<Double> getContribution() {
        showOnceAndWait();
        return controller.getContribution();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WizardPage<Optional<Double>> getWizardPage() {
        try {
            Pane root = loadFXML("Contribution_Wizard.fxml");
            return new WizardPage<>(root, null, false,
                    () -> controller.getContribution(),
                    controller.validProperty());
        } catch (IOException ex) {
            Logger.getLogger(Contribution.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
