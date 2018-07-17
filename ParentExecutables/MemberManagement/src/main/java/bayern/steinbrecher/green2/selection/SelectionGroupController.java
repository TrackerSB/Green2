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

import bayern.steinbrecher.green2.WizardableController;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.utility.BindingUtility;
import com.google.common.collect.BiMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * The controller of the SelectionGroup.
 *
 * @author Stefan Huber
 * @see SelectionGroup
 * @param <T> The type of the options to select.
 * @param <G> The type of the groups to associate items with.
 */
public class SelectionGroupController<T extends Comparable<T>, G> extends WizardableController<Optional<Map<T, G>>> {

    private final ObservableValue<ObservableList<AssociatedItem>> options
            = new SimpleObjectProperty<>(this, "options",
                    FXCollections.observableArrayList(i -> new Observable[]{i.itemProperty(), i.groupProperty()}));
    private final IntegerProperty totalCount = new SimpleIntegerProperty(this, "totalCount");
    //FIXME Since the value should also be unique it should be something like a BiMapProperty.
    private final MapProperty<G, Color> groups
            = new SimpleMapProperty<>(this, "groups", FXCollections.observableHashMap());
    private final ObjectProperty<Optional<G>> currentGroup
            = new SimpleObjectProperty<>(this, "selectedGroup", Optional.empty());
    private final BooleanProperty currentGroupSelected = new SimpleBooleanProperty(this, "currentGroupSelected");
    private final MapProperty<G, IntegerProperty> selectedPerGroup
            = new SimpleMapProperty<>(this, "selectedPerGroup", FXCollections.observableHashMap());
    private final IntegerProperty selectedCount = new SimpleIntegerProperty(this, "selectedCount", 0);
    private final BooleanProperty nothingSelected = new SimpleBooleanProperty(this, "nothingSelected", true);
    private final BooleanProperty allSelected = new SimpleBooleanProperty(this, "allSelected");
    @FXML
    private ListView<AssociatedItem> optionsListView;
    @FXML
    private ToggleGroup groupsToggleGroup;
    @FXML
    private VBox groupsBox;
    private RadioButton unselectGroup;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public void initialize(URL location, ResourceBundle resources) {
        bindValidProperty(nothingSelected.not());
        nothingSelected.bind(selectedCount.lessThanOrEqualTo(0));
        allSelected.bind(selectedCount.greaterThanOrEqualTo(totalCount));
        currentGroup.addListener((obs, oldVal, newVal) -> currentGroupSelected.set(newVal.isPresent()));

        optionsListView.itemsProperty().bind(options);
        optionsListView.setCellFactory(listview -> new ListCell<AssociatedItem>() {

            @Override
            protected void updateItem(AssociatedItem item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    setText(item.getItem().toString());
                    Optional<G> newGroup = item.getGroup();

                    //TODO Spare creations of CheckBoxes
                    CheckBox groupGraphic = new CheckBox();
                    groupGraphic.setSelected(newGroup.isPresent());
                    groupGraphic.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        //TODO Find a simplyfied boolean expression
                        if (newVal) {
                            //Selected
                            item.setGroup(currentGroup.get());
                        } else {
                            if (currentGroup.get().equals(newGroup) || !currentGroup.get().isPresent()) {
                                //Unselected
                                item.setGroup(Optional.empty());
                            } else {
                                //Reselect
                                item.setGroup(currentGroup.get());
                            }
                        }
                        updateItem(item, empty);
                    });

                    if (newGroup.isPresent()) {
                        Color fill = groups.get(newGroup.get());
                        List<Region> boxes = groupGraphic.lookupAll(".box")
                                .stream()
                                //There should be only boxes (which are regions)
                                .filter(node -> node instanceof Region)
                                .map(node -> (Region) node)
                                .collect(Collectors.toList());
                        boxes.forEach(region -> region.setBackground(
                                new Background(new BackgroundFill(fill, CornerRadii.EMPTY, Insets.EMPTY))));
                        groupGraphic.getChildrenUnmodifiable()
                                .addListener((ListChangeListener.Change<? extends Node> change) -> {
                                    while (change.next()) {
                                        change.getAddedSubList()
                                                .stream()
                                                .filter(node -> node.getStyleClass().contains("box"))
                                                .filter(node -> node instanceof Region)
                                                .map(node -> (Region) node)
                                                //CHECKSTYLE.OFF: MagicNumber - The range of RGB goes from 0 to 255
                                                .forEach(region -> region.setStyle("-fx-background-color: rgba("
                                                + (255 * fill.getRed()) + ", " + (255 * fill.getGreen()) + ", "
                                                + (255 * fill.getBlue()) + ", " + fill.getOpacity() + ")"));
                                        //CHECKSTYLE.ON: MagicNumber
                                    }
                                });
                    }
                    setGraphic(groupGraphic);
                }
            }
        });

        unselectGroup = addGroupRadioButton(EnvironmentHandler.getResourceValue("unselect"), Optional.empty(), false);
        selectedPerGroup.addListener((MapChangeListener.Change<? extends G, ? extends IntegerProperty> change) -> {
            selectedCount.bind(BindingUtility.reduceSum(selectedPerGroup.values().stream()));
        });

        groups.addListener((MapChangeListener.Change<? extends G, ? extends Color> change) -> {
            G group = change.getKey();
            if (change.wasAdded()) {
                selectedPerGroup.put(group, new SimpleIntegerProperty(0));
                RadioButton groupRadioButton
                        = addGroupRadioButton(change.getKey().toString(), Optional.of(group), false);
                if (groups.size() == 1) { //NOPMD - Select the first added group.
                    groupRadioButton.setSelected(true);
                }
            }
            if (change.wasRemoved()) {
                selectedPerGroup.remove(change.getKey());
                //Remove RadioButton representing the group
                Color removedColor = groups.get(group);
                groupsToggleGroup.getToggles().stream()
                        //Per construction Toggles are always Radiobuttons
                        .filter(toggle -> toggle instanceof RadioButton)
                        .map(toggle -> (RadioButton) toggle)
                        .filter(radiobutton -> {
                            Node graphic = radiobutton.getGraphic();
                            //Per construction graphics are always Rectangles
                            return graphic instanceof Rectangle && ((Rectangle) graphic).getFill().equals(removedColor);
                        })
                        //FIXME Without BiMapProperty the number of found radiobuttons is not limited to 1.
                        .findAny()
                        .ifPresentOrElse(
                                radiobutton -> {
                                    if (radiobutton.isSelected()) {
                                        unselectGroup.setSelected(true);
                                    }
                                    groupsToggleGroup.getToggles().remove(radiobutton);
                                },
                                () -> Logger.getLogger(SelectionGroupController.class.getName())
                                        .log(Level.WARNING, "Could not find the group radiobutton to remove."));

                //Remove all associations of items with this group.
                options.getValue().stream()
                        .filter(ai -> ai.getGroup().equals(group))
                        .forEach(ai -> ai.setGroup(Optional.empty()));
            }
        });

        options.getValue().addListener((ListChangeListener.Change<? extends AssociatedItem> change) -> {
            while (change.next()) {
                totalCount.set(totalCount.get() + change.getAddedSize() - change.getRemovedSize());
                change.getAddedSubList().stream()
                        .forEach(item -> {
                            item.groupProperty().addListener((obs, oldVal, newVal) -> {
                                oldVal.ifPresent(oldGroup -> {
                                    IntegerProperty countProperty = selectedPerGroup.get(oldGroup);
                                    countProperty.set(countProperty.get() - 1);
                                });
                                newVal.ifPresent(newGroup -> {
                                    IntegerProperty countProperty = selectedPerGroup.get(newGroup);
                                    countProperty.set(countProperty.get() + 1);
                                });
                            });
                        });
            }
        });
        totalCount.set(options.getValue().size());

        HBox.setHgrow(optionsListView, Priority.ALWAYS);
    }

    private RadioButton addGroupRadioButton(String text, Optional<G> group, boolean setSelected) {
        RadioButton radioButton = new RadioButton(text);
        radioButton.textProperty().bind(new SimpleStringProperty(text).concat(" (")
                .concat(group.isPresent() ? selectedPerGroup.get(group.get()) : totalCount.subtract(selectedCount))
                .concat(")"));
        double fontSize = radioButton.getFont().getSize();
        radioButton.setGraphic(new Rectangle(
                fontSize, fontSize, group.isPresent() ? groups.get(group.get()) : Color.TRANSPARENT));
        radioButton.setToggleGroup(groupsToggleGroup);
        radioButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                currentGroup.set(group);
            }
        });
        radioButton.setSelected(setSelected);
        groupsBox.getChildren().add(radioButton);
        return radioButton;
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void select() {
        if (isValid()) {
            getStage().close();
        }
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void selectAllOptions() {
        options.getValue().stream()
                .forEach(option -> {
                    if (!option.isAssociated()) {
                        option.setGroup(currentGroup.get());
                    }
                });
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void selectNoOption() {
        options.getValue().stream().forEach(option -> {
            /*
             * NOTE When only deselecting once it may be reselected since the user can change the group association
             * directly without explicitely deselecting an item.
             */
            //TODO Check if this is really true
            option.setGroup(Optional.empty());
            option.setGroup(Optional.empty());
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<Map<T, G>> calculateResult() {
        Map<T, G> selection = options.getValue().stream()
                .filter(entry -> entry.getGroup().isPresent())
                .collect(Collectors.toMap(AssociatedItem::getItem, entry -> entry.getGroup().get()));
        return Optional.of(selection);
    }

    /**
     * Sets a new set of options. This clears all current options and resets the selected group to "unselect".
     *
     * @param options The new options to show.
     */
    public void setOptions(Set<T> options) {
        this.options.getValue().clear();
        options.stream().sorted().forEach(o -> this.options.getValue().add(new AssociatedItem(o)));
    }

    /**
     * Returns the currently set options.
     *
     * @return The currently set options.
     */
    public Set<T> getOptions() {
        return options.getValue().stream()
                .map(AssociatedItem::getItem)
                .collect(Collectors.toSet());
    }

    /**
     * Sets a new set of groups. This clears all groups but the unselect group and clears all selections of any option.
     *
     * @param groups The new groups to set.
     */
    public void setGroups(BiMap<G, Color> groups) {
        this.groups.clear();
        this.groups.putAll(groups);
    }

    /**
     * Returns the currently set groups but the unselect group.
     *
     * @return The currently set groups but the unselect group.
     */
    public Set<G> getGroups() {
        return groups.keySet();
    }

    /**
     * Returns the property holding whether a group is selected which is not the unselect group.
     *
     * @return The property holding whether a group is selected which is not the unselect group.
     */
    public ReadOnlyBooleanProperty currentGroupSelectedProperty() {
        return currentGroupSelected;
    }

    /**
     * Checks a group is selected which is not the unselect group.
     *
     * @return {@code true} if and only if a group is selected which is not the unselect group.
     */
    public boolean isCurrentGroupSelected() {
        return currentGroupSelectedProperty().get();
    }

    /**
     * Returns the property representing the number of items associated with a group.
     *
     * @return The property representing the number of items associated with a group.
     */
    public ReadOnlyMapProperty<G, ? extends ReadOnlyIntegerProperty> selectedPerGroupProperty() {
        return selectedPerGroup;
    }

    /**
     * Returns the mapping between each group and the number of options associated with it.
     *
     * @return The mapping between each group and the number of options associated with it.
     */
    public ObservableMap<G, ? extends ReadOnlyIntegerProperty> getSelectedPerGroup() {
        return selectedPerGroupProperty().getValue();
    }

    /**
     * Returns the property representing the total number of entries to select.
     *
     * @return The property representing the total number of entries to select.
     */
    public ReadOnlyIntegerProperty totalCountProperty() {
        return totalCount;
    }

    /**
     * The total number of entries to select.
     *
     * @return The total number of entries to select.
     */
    public int getTotalCount() {
        return totalCount.get();
    }

    /**
     * Returns the property representing a boolean indicating whether no entry is currently selected.
     *
     * @return The property representing a boolean indicating whether no entry is currently selected.
     */
    public ReadOnlyBooleanProperty nothingSelectedProperty() {
        return nothingSelected;
    }

    /**
     * Checks whether currently no entry is selected.
     *
     * @return {@code true} only if no entry is selected.
     */
    public boolean isNothingSelected() {
        return nothingSelected.get();
    }

    /**
     * Returns the property representing a boolean indicating whether every entry is currently selected.
     *
     * @return The property representing a boolean indicating whether every entry is currently selected.
     */
    public ReadOnlyBooleanProperty allSelectedProperty() {
        return allSelected;
    }

    /**
     * Checks whether currently every entry is selected.
     *
     * @return {@code true} only if every entry is selected.
     */
    public boolean isAllSelected() {
        return allSelected.get();
    }

    /**
     * Represents a relation between an option to select and an group it is associated with.
     */
    private class AssociatedItem {

        private final ObjectProperty<T> item = new SimpleObjectProperty<>();
        private final ObjectProperty<Optional<G>> group = new SimpleObjectProperty<>(Optional.empty());

        AssociatedItem(T item) {
            this.item.set(item);
        }

        /**
         * Returns the property holding the item to select.
         *
         * @return The property holding the item to select.
         */
        public ReadOnlyObjectProperty<T> itemProperty() {
            return item;
        }

        /**
         * Returns the item which can be selected.
         *
         * @return The item which can be selected.
         */
        public T getItem() {
            return itemProperty().get();
        }

        /**
         * Returns the property which holds the group this item is currently associated with.
         *
         * @return The property which holds the group this item is currently associated with.
         */
        public ObjectProperty<Optional<G>> groupProperty() {
            return group;
        }

        /**
         * Returns the group this item is currently associated with.
         *
         * @return The group this item is currently associated with.
         */
        public Optional<G> getGroup() {
            return groupProperty().get();
        }

        /**
         * Changes the group this item is currently associated with.
         *
         * @param group The group this item is currently associated with. {@link Optional#empty()} signals that this
         * item not associated with any group.
         */
        public void setGroup(Optional<G> group) {
            if (!group.isPresent() || getGroups().contains(group.get())) {
                this.group.set(group);
            }
        }

        /**
         * Checks whether this item is currently associated with a group.
         *
         * @return {@code true} only if this item is currently associated with a group.
         */
        public boolean isAssociated() {
            return group.get().isPresent();
        }
    }
}
