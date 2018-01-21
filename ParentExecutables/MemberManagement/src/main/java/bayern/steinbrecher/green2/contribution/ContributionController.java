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
package bayern.steinbrecher.green2.contribution;

import bayern.steinbrecher.green2.WizardableController;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.elements.spinner.ContributionField;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Contains a window for inserting a double value representing a contribution.
 *
 * @author Stefan Huber
 */
public class ContributionController extends WizardableController {

    private static final List<Color> PREDEFINED_COLORS = List.of(Color.FORESTGREEN, Color.rgb(232, 181, 14),
            Color.rgb(255, 48, 28), Color.rgb(78, 14, 232), Color.rgb(16, 255, 234), Color.rgb(135, 139, 38),
            Color.rgb(232, 115, 21), Color.rgb(246, 36, 255), Color.rgb(23, 115, 232), Color.rgb(24, 255, 54));
    private static final Random COLOR_RANDOM = new Random();
    @FXML
    private VBox contributionFieldsBox;
    private ListProperty<ContributionField> contributionFields
            = new SimpleListProperty<>(this, "contributionSpinner", FXCollections.observableArrayList());
    private BooleanProperty uniqueColors = new SimpleBooleanProperty(this, "uniqueColors", true);
    private final ChangeListener<Object> calculateUniqueColors = (obs, oldVal, newVal) -> {
        //Check for duplicate colors
        Set<Color> colors = new HashSet<>();
        uniqueColors.set(contributionFields.stream().allMatch(cf -> colors.add(cf.getColor())));
    };
    private BooleanProperty allContributionFieldsValid
            = new SimpleBooleanProperty(this, "allContributionFieldsValid", true);
    private final ChangeListener<Object> calculateAllContributionFieldsValid = (obs, oldVal, newVal) -> {
        allContributionFieldsValid.set(contributionFields.stream().allMatch(ContributionField::isValid));
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contributionFields.addListener(calculateUniqueColors);
        contributionFields.addListener(calculateAllContributionFieldsValid);
        contributionFields.addListener((ListChangeListener.Change<? extends ContributionField> change) -> {
            while (change.next()) {
                change.getAddedSubList().forEach(addedCf -> {
                    addedCf.getColorPicker().setValue(PREDEFINED_COLORS.stream()
                            .sequential()
                            .filter(
                                    predefColor -> contributionFields.stream()
                                            .map(ContributionField::getColorPicker)
                                            .map(ColorPicker::getValue)
                                            .noneMatch(c -> c.equals(predefColor)))
                            .findFirst()
                            .orElse(Color.rgb(COLOR_RANDOM.nextInt(256),
                                    COLOR_RANDOM.nextInt(256), COLOR_RANDOM.nextInt(256))));
                    addedCf.getContributionSpinner().getEditor().setOnAction(aevt -> submitContributions());
                    contributionFieldsBox.getChildren().add(createContributionRow(addedCf));
                    addedCf.colorProperty().addListener(calculateUniqueColors);
                    addedCf.getContributionSpinner().validProperty().addListener(calculateAllContributionFieldsValid);
                });
                change.getRemoved().forEach(removedCf -> {
                    List<HBox> hboxes = contributionFieldsBox.getChildren().stream()
                            //If working as expected there should only be objects of the class HBox
                            .filter(node -> node instanceof HBox)
                            .map(node -> (HBox) node)
                            .filter(hbox -> hbox.getChildren().contains(removedCf))
                            .distinct() //TODO Think about whether this is needed
                            .collect(Collectors.toList());
                    if (hboxes.isEmpty()) {
                        Logger.getLogger(ContributionController.class.getName())
                                .log(Level.WARNING, "Could not remove row containing the removed ContributionField.");
                    } else {
                        if (hboxes.size() > 1) {
                            Logger.getLogger(ContributionController.class.getName()).log(Level.WARNING,
                                    "Found multiple rows containing the removed ContributionField.\n"
                                    + "Only first one got removed.");
                        }
                        contributionFieldsBox.getChildren().remove(hboxes.get(0));
                    }
                });
            }
        });
        valid.bind(allContributionFieldsValid.and(uniqueColors));
        addContributionField();
    }

    private HBox createContributionRow(ContributionField cf) {
        String remove = EnvironmentHandler.getResourceValue("remove");
        Button removeButton = new Button(remove);
        removeButton.disableProperty().bind(contributionFields.sizeProperty().lessThanOrEqualTo(1));
        removeButton.setOnAction(aevt -> removeContributionField(cf));
        return new HBox(cf, removeButton);
    }

    @FXML
    private void addContributionField() {
        contributionFields.add(new ContributionField());
    }

    @FXML
    private void removeContributionField(ContributionField contributionField) {
        contributionFields.remove(contributionField);
    }

    @FXML
    private void submitContributions() {
        checkStage();
        if (valid.get()) {
            stage.close();
        }
    }

    /**
     * Returns the currently inserted contributions. Returns {@link Optional#empty()} if the user didn't confirm the
     * contribution yet or the input is invalid.
     *
     * @return The currently inserted contributions and their colors.
     */
    public Optional<Map<Color, Double>> getContribution() {
        if (userAbborted() || !valid.get()) {
            return Optional.empty();
        } else {
            Map<Color, Double> contributions = new HashMap<>(contributionFields.getSize());
            contributionFields.forEach(cf -> contributions.put(cf.getColor(), cf.getContribution()));
            return Optional.of(contributions);
        }
    }
}
