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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * The controller of the SelectionGroup.
 *
 * @author Stefan Huber
 * @see SelectionGroup
 * @param <T> The type of the options to select.
 */
public class SelectionGroupController<T extends Comparable<T>> extends WizardableController {

    private static final int DEFAULT_RECT_SIZE = 10;
    private final MapProperty<T, CheckBoxGroupPair> options
            = new SimpleMapProperty<>(this, "options", FXCollections.observableHashMap());
    private final ReadOnlyIntegerProperty totalCount = options.sizeProperty();
    private final MapProperty<Color, Object> groups //FIXME Try using ? or _ in JDK 9 instead of Object
            = new SimpleMapProperty<>(this, "groups", FXCollections.observableHashMap());
    private final ObjectProperty<Optional<Color>> currentGroup
            = new SimpleObjectProperty<>(this, "selectedGroup", Optional.empty());
    private final MapProperty<Color, IntegerProperty> selectedPerGroup
            = new SimpleMapProperty<>(this, "selectedPerGroup", FXCollections.observableHashMap());
    private final IntegerProperty selectedCount = new SimpleIntegerProperty(this, "selectedCount", 0);
    private final BooleanProperty nothingSelected = new SimpleBooleanProperty(this, "nothingSelected", true);
    private final BooleanProperty allSelected = new SimpleBooleanProperty(this, "allSelected");
    @FXML
    private ListView<CheckBox> optionsListView;
    @FXML
    private ToggleGroup groupsToggleGroup;
    @FXML
    private VBox groupsBox;
    private RadioButton unselectGroup;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        valid.bind(nothingSelected.not());
        nothingSelected.bind(selectedCount.lessThanOrEqualTo(0));
        allSelected.bind(selectedCount.greaterThanOrEqualTo(options.sizeProperty()));

        unselectGroup = addGroupRadioButton(EnvironmentHandler.getResourceValue("unselect"), Optional.empty(), false);
        selectedPerGroup.addListener((MapChangeListener.Change<? extends Color, ? extends IntegerProperty> change) -> {
            selectedCount.bind(BindingUtility.reduceSum(selectedPerGroup.values().stream()));
        });

        options.addListener((MapChangeListener.Change<? extends T, ? extends CheckBoxGroupPair> change) -> {
            if (change.wasAdded()) {
                if (!change.getValueAdded().getCheckbox().isPresent()) {
                    CheckBox checkbox = new CheckBox(change.getKey().toString());
                    checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            applyCurrentGroupToCheckBox(change.getKey());
                            checkbox.setSelected(currentGroup.get().isPresent());
                        } else {
                            boolean setSelected = currentGroup.get().isPresent()
                                    && !options.get(change.getKey()).getColor().equals(currentGroup.get());

                            Optional<Color> previous = currentGroup.get();
                            currentGroup.set(Optional.empty());
                            applyCurrentGroupToCheckBox(change.getKey());
                            currentGroup.set(previous);

                            checkbox.setSelected(setSelected);
                        }
                    });
                    options.get(change.getKey()).setCheckbox(Optional.of(checkbox));
                    optionsListView.getItems().add(checkbox);
                }
            }
            if (change.wasRemoved()) { //TODO Check whether removing checkboxes works
                //Remove from selectedPerGroup
                options.forEach((key, pair) -> {
                    if (pair.equals(change.getValueRemoved())) {
                        IntegerProperty count = selectedPerGroup.get(pair.getColor().get());
                        count.set(count.get() - 1);
                    }
                });

                //Remove from ListView
                List<CheckBox> checkboxes = optionsListView.getItems().stream()
                        .filter(cb -> cb.getText().equals(change.getKey().toString()))
                        .collect(Collectors.toList());
                if (checkboxes.isEmpty()) {
                    Logger.getLogger(SelectionGroupController.class.getName())
                            .log(Level.WARNING, "Could not remove row of removed CheckBox.");
                } else {
                    if (checkboxes.size() > 1) {
                        Logger.getLogger(SelectionGroupController.class.getName())
                                .log(Level.WARNING, "Found multiple rows which contain CheckBox.\n"
                                        + "Only the first got removed.");
                    }
                    optionsListView.getItems().remove(checkboxes.get(0));
                }
            }
        });
        groups.addListener((MapChangeListener.Change<? extends Color, ?> change) -> {
            if (change.wasAdded()) {
                selectedPerGroup.put(change.getKey(), new SimpleIntegerProperty(0));
                addGroupRadioButton(change.getValueAdded().toString(), Optional.of(change.getKey()), false);
                if (groups.size() == 1) {
                    Optional<RadioButton> aloneSelectGroup = streamGroupsRadioButtons()
                            .filter(rb -> !rb.equals(unselectGroup))
                            .findAny();
                    aloneSelectGroup.get().setSelected(true);
                }
            }
            if (change.wasRemoved()) { //TODO Check whether removing radiobuttons works
                List<RadioButton> radiobuttons = streamGroupsRadioButtons()
                        .filter(rb -> rb.getText().equals(change.getValueRemoved().toString()))
                        .collect(Collectors.toList());
                if (radiobuttons.isEmpty()) {
                    Logger.getLogger(SelectionGroupController.class.getName())
                            .log(Level.WARNING, "Could not remove row of removed CheckBox.");
                } else {
                    if (radiobuttons.size() > 1) {
                        Logger.getLogger(SelectionGroupController.class.getName())
                                .log(Level.WARNING, "Found multiple rows which contain CheckBox.\n"
                                        + "Only the first got removed.");
                    }
                    RadioButton radiobuttonToRemove = radiobuttons.get(0);
                    groupsBox.getChildren().remove(radiobuttonToRemove);
                    streamGroupsRadioButtons().findFirst()
                            .orElse(unselectGroup)
                            .setSelected(true);
                }
                selectedPerGroup.remove(change.getKey());
            }
        });

        HBox.setHgrow(optionsListView, Priority.ALWAYS);
    }

    private Stream<RadioButton> streamGroupsRadioButtons() {
        return groupsBox.getChildren().stream()
                .filter(n -> n instanceof RadioButton)
                .map(n -> (RadioButton) n);
    }

    private void applyCurrentGroupToCheckBox(T key) {
        Optional<Color> optNewColor = currentGroup.get();
        CheckBoxGroupPair valuePair = options.get(key);
        Optional<Color> optOldColor = valuePair.getColor();

        optOldColor.ifPresent(color -> selectedPerGroup.get(color).set(selectedPerGroup.get(color).get() - 1));
        optNewColor.ifPresent(color -> selectedPerGroup.get(color).set(selectedPerGroup.get(color).get() + 1));

        valuePair.setColor(optNewColor);
        assert valuePair.getCheckbox().isPresent() : "CheckBox has to be set before calling setColorToCheckBox";
        if (optNewColor.isPresent()) {
            //TODO Set style only for elements necessary
            Color newColor = optNewColor.get();
            valuePair.getCheckbox().get().getChildrenUnmodifiable().stream()
                    .forEach(node -> node.setStyle(new StringJoiner(", ", "-fx-background-color: rgba(", ")")
                    .add(Double.toString(255 * newColor.getRed()))
                    .add(Double.toString(255 * newColor.getGreen()))
                    .add(Double.toString(255 * newColor.getBlue()))
                    .add(Double.toString(newColor.getOpacity()))
                    .toString()));
        } else {
            //TODO Set style only for elements necessary
            valuePair.getCheckbox().get().getChildrenUnmodifiable().forEach(node -> node.setStyle(""));
        }
    }

    private RadioButton addGroupRadioButton(String text, Optional<Color> color, boolean setSelected) {
        RadioButton radioButton = new RadioButton(text);
        radioButton.textProperty().bind(new SimpleStringProperty(text).concat(" (")
                .concat(color.isPresent() ? selectedPerGroup.get(color.get()) : totalCount.subtract(selectedCount))
                .concat(")"));
        radioButton.setGraphic(new Rectangle(DEFAULT_RECT_SIZE, DEFAULT_RECT_SIZE, color.orElse(Color.TRANSPARENT)));
        radioButton.setToggleGroup(groupsToggleGroup);
        radioButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                currentGroup.set(color);
            }
        });
        radioButton.setSelected(setSelected);
        groupsBox.getChildren().add(radioButton);
        return radioButton;
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void select() {
        checkStage();
        if (valid.get()) {
            stage.close();
        }
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void selectAllOptions() {
        optionsListView.getItems().stream()
                .filter(cb -> !cb.isSelected())
                .forEach(cb -> cb.setSelected(true)); //TODO May be parallel?
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void selectNoOption() {
        optionsListView.getItems().stream()
                .forEach(cb -> cb.setSelected(false));
        //When unselecting a checkbox it may reselect itself if the currently chosen group differs from its own group.
        //So the options have to be unselected a second time.
        optionsListView.getItems().stream()
                .forEach(cb -> cb.setSelected(false));
    }

    /**
     * Returns the list of currently selected items. Returns {@link Optional#empty()} if the user didn't confirm the
     * selection yet.
     *
     * @return An {@link Optional} containing the selection if any.
     */
    public Optional<Map<T, Color>> getSelection() {
        if (userAbborted()) {
            return Optional.empty();
        } else {
            Map<T, Color> selection = options.entrySet().stream()
                    .filter(entry -> entry.getValue().getColor().isPresent())
                    .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getColor().get()));
            return Optional.of(selection);
        }
    }

    /**
     * Sets a new set of options. This clears all current options and resets the selected group to "unselect".
     *
     * @param options The new options to show.
     */
    public void setOptions(Set<T> options) {
        this.options.clear();
        options.stream().sorted().forEach(o -> this.options.put(o, new CheckBoxGroupPair()));
    }

    /**
     * Returns the currently set options.
     *
     * @return The currently set options.
     */
    public Set<T> getOptions() {
        return options.keySet();
    }

    /**
     * Sets a new set of groups. This clears all groups but the unselect group and clears all selections of any option.
     *
     * @param groups The new groups to set.
     */
    public void setGroups(Map<Color, ?> groups) {
        this.groups.clear();
        this.groups.putAll(groups);
    }

    /**
     * Returns the currently set groups but the unselect group.
     *
     * @return The currently set groups but the unselect group.
     */
    public Set<Color> getGroups() {
        return groups.keySet();
    }

    /**
     * Returns the property representing the currently set groups.
     *
     * @return The property representing the currently set groups.
     */
    public ReadOnlyMapProperty<Color, ? extends ReadOnlyIntegerProperty> selectedPerGroupProperty() {
        return selectedPerGroup;
    }

    /**
     * Returns the mapping between each group and the number of options associated with it.
     *
     * @return The mapping between each group and the number of options associated with it.
     */
    public ObservableMap<Color, ? extends ReadOnlyIntegerProperty> getSelectedPerGroup() {
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

    private class CheckBoxGroupPair {

        private Optional<Color> color;
        private Optional<CheckBox> checkbox;

        public CheckBoxGroupPair() {
            this(Optional.empty(), Optional.empty());
        }

        public CheckBoxGroupPair(Optional<Color> color, Optional<CheckBox> checkbox) {
            this.color = color;
            this.checkbox = checkbox;
        }

        public Optional<Color> getColor() {
            return color;
        }

        public void setColor(Optional<Color> color) {
            this.color = color;
        }

        public Optional<CheckBox> getCheckbox() {
            return checkbox;
        }

        public void setCheckbox(Optional<CheckBox> checkbox) {
            this.checkbox = checkbox;
        }

        public void setValues(Optional<Color> color, Optional<CheckBox> checkbox) {
            setColor(color);
            setCheckbox(checkbox);
        }
    }

    private class LabelCountPair {

        private Optional<Label> label;
        private final IntegerProperty count;

        public LabelCountPair() {
            this(Optional.empty(), 0);
        }

        public LabelCountPair(Optional<Label> label, int initialCount) {
            this.label = label;
            this.count = new SimpleIntegerProperty(this, "count", initialCount);
        }

        public Optional<Label> getLabel() {
            return label;
        }

        public void setLabel(Optional<Label> label) {
            this.label = label;
        }

        public IntegerProperty getCount() {
            return count;
        }
    }
}