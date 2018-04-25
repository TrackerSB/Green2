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
package bayern.steinbrecher.green2.selection;

import bayern.steinbrecher.green2.WizardableView;
import com.google.common.collect.BiMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Represents a selection which allows to only select options but also to group them.
 *
 * @author Stefan Huber
 * @param <T> The type of the options to select.
 * @param <G> The type of the groups to associate items with.
 */
public final class SelectionGroup<T extends Comparable<T>, G>
        extends WizardableView<Optional<Map<T, G>>, SelectionGroupController<T, G>> {

    private final Set<T> options;
    private final BiMap<G, Color> groups;

    /**
     * Creates a new SelectionGroup for selecting and grouping the given options into the given groups.
     *
     * @param options The options to select.
     * @param groups The groups to group the options into.
     */
    public SelectionGroup(Set<T> options, BiMap<G, Color> groups) {
        this.options = options;
        this.groups = groups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void callWhenLoadFXML() {
        getController().setGroups(groups);
        getController().setOptions(options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startImpl(Stage primaryStage) {
        throw new UnsupportedOperationException(
                "Currently the SelectionGroup dialog can only be used within a Wizard.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getWizardFxmlPath() {
        return "SelectionGroupWizard.fxml";
    }
}
