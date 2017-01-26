/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.selection;

import bayern.steinbrecher.green2.WizardableView;
import bayern.steinbrecher.green2.data.DataProvider;
import bayern.steinbrecher.wizard.WizardPage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a selection dialog.
 *
 * @param <T> The type of the attributes being able to selct.
 * @author Stefan Huber
 */
public class Selection<T extends Comparable<T>>
        extends WizardableView<Optional<List<T>>, SelectionController<T>> {

    private final List<T> options;
    private Stage owner;

    /**
     * Greates a new Frame representing the given options as selectable
     * {@code CheckBox}es and representing a {@code TextField} for entering a
     * number.
     *
     * @param options The options the user is allowed to select.
     */
    public Selection(List<T> options) {
        this(options, null);
    }

    /**
     * Greates a new Frame representing the given options as selectable
     * {@code CheckBox}es and representing a {@code TextField} for entering a
     * number.
     *
     * @param options The options the user is allowed to select.
     * @param owner   The owner which causes this window to close if the owner is
     *                closed. The owner is not blocked.
     */
    public Selection(List<T> options, Stage owner) {
        this.options = options;
        this.owner = owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <P extends Parent> P loadFXML(String resource)
            throws IOException {
        P root = super.loadFXML(resource);
        controller.setOptions(options);
        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        Parent root = loadFXML("Selection.fxml");
        controller.setStage(stage);

        stage.initOwner(owner);
        stage.setScene(new Scene(root));
        stage.setTitle(DataProvider.getResourceValue("selectionTitle"));
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.ImageSet.LOGO.get());
    }

    /**
     * Retruns the list of currently selected items. Returns
     * {@code Optional.empty} if the user abborted the selection.
     *
     * @return The selection if any.
     */
    public Optional<List<T>> getSelection() {
        return controller.getSelection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WizardPage<Optional<List<T>>> getWizardPage() {
        try {
            Pane root = loadFXML("Selection_Wizard.fxml");
            return new WizardPage<>(root, null, false,
                    () -> controller.getSelection(),
                    controller.validProperty());
        } catch (IOException ex) {
            Logger.getLogger(Selection.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }
}