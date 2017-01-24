/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.selection;

import bayern.steinbrecher.gruen2.WizardableView;
import bayern.steinbrecher.gruen2.data.DataProvider;
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
 * @author Stefan Huber
 * @param <T> The type of the attributes being able to selct.
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
     *
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
     * @param owner The owner which causes this window to close if the owner is
     * closed. The owner is not blocked.
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
        stage.getIcons().add(DataProvider.DEFAULT_ICON);
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
