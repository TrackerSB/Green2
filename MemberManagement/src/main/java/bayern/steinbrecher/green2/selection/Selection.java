/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.selection;

import bayern.steinbrecher.green2.WizardableView;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.WizardPage;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Represents a selection dialog.
 *
 * @param <T> The type of the attributes being able to select.
 * @author Stefan Huber
 */
public class Selection<T extends Comparable<T>> extends WizardableView<Optional<List<T>>, SelectionController<T>> {

    private final List<T> options;
    private Stage owner;

    /**
     * Creates a new Frame representing the given options as selectable {@link CheckBox} es and representing a
     * {@link TextField} for entering a number.
     *
     * @param options The options the user is allowed to select.
     */
    public Selection(List<T> options) {
        this(options, null);
    }

    /**
     * Creates a new Frame representing the given options as selectable {@link CheckBox}es and representing a
     * {@link TextField} for entering a number.
     *
     * @param options The options the user is allowed to select.
     * @param owner The owner which causes this window to close if the owner is closed. The owner is not blocked.
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
        stage.setTitle(EnvironmentHandler.getResourceValue("selectionTitle"));
        stage.setResizable(false);
        stage.getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());
    }

    /**
     * Returns the list of currently selected items. Returns {@link Optional#empty()} if the user aborted the selection.
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