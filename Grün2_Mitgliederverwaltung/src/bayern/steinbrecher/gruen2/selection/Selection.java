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
package bayern.steinbrecher.gruen2.selection;

import bayern.steinbrecher.gruen2.View;
import bayern.steinbrecher.gruen2.data.DataProvider;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents a selection dialog.
 *
 * @author Stefan Huber
 * @param <T> The type of the attributes being able to selct.
 */
public class Selection<T extends Comparable>
        extends View<SelectionController<T>> {

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
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource("Selection.fxml"));
        fxmlLoader.setResources(DataProvider.RESOURCE_BUNDLE);
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.STYLESHEET_PATH);

        controller = fxmlLoader.getController();
        controller.setStage(stage);
        controller.setOptions(options);

        stage.initOwner(owner);
        stage.setScene(new Scene(root));
        stage.setTitle(DataProvider.getResourceValue("selectionTitle"));
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.DEFAULT_ICON);
    }

    /**
     * Opens the selection window if no other process yet opened one, blocks
     * until the window is closed and retruns the list of items which were
     * selected. Returns {@code Optional.empty} if the user did not confirm the
     * selection. The window will only be opened ONCE; even if multiple threads
     * are calling this function. They will be blocked until the window is
     * closed.
     *
     * @return The selection if any.
     */
    public Optional<List<T>> getSelection() {
        showOnceAndWait();
        return controller.getSelection();
    }
}
