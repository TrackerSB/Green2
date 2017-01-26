/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.contribution;

import bayern.steinbrecher.green2.WizardableView;
import bayern.steinbrecher.green2.data.DataProvider;
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
     *              closed. The owner is not blocked.
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
        stage.getIcons().add(DataProvider.ImageSet.LOGO.get());
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
