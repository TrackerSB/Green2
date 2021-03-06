package bayern.steinbrecher.green2.memberManagement.contribution;

import bayern.steinbrecher.checkedElements.report.ReportEntry;
import bayern.steinbrecher.checkedElements.report.ReportType;
import bayern.steinbrecher.checkedElements.spinner.CheckedDoubleSpinner;
import bayern.steinbrecher.checkedElements.spinner.ContributionField;
import bayern.steinbrecher.wizard.WizardPageController;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Contains a window for inserting a double value representing a contribution.
 *
 * @author Stefan Huber
 */
public class ContributionController extends WizardPageController<Optional<BiMap<Double, Color>>> {

    private static final Logger LOGGER = Logger.getLogger(ContributionController.class.getName());
    private static final List<Color> PREDEFINED_COLORS = List.of(Color.FORESTGREEN, Color.rgb(232, 181, 14),
            Color.rgb(255, 48, 28), Color.rgb(78, 14, 232), Color.rgb(16, 255, 234), Color.rgb(135, 139, 38),
            Color.rgb(232, 115, 21), Color.rgb(246, 36, 255), Color.rgb(23, 115, 232), Color.rgb(24, 255, 54));
    private static final Random COLOR_RANDOM = new Random();
    @FXML
    private VBox contributionFieldsBox;
    @FXML
    private ResourceBundle resources;
    private final ListProperty<DuplicateContributionField> contributionFields
            = new SimpleListProperty<>(this, "contributionSpinner", FXCollections.observableArrayList());
    private final BooleanProperty allContributionFieldsValid
            = new SimpleBooleanProperty(this, "allContributionFieldsValid", true);
    private final ChangeListener<Object> calculateAllContributionFieldsValid = (obs, oldVal, newVal) -> {
        boolean areAllValid = contributionFields.stream()
                .allMatch(ContributionField::isValid);
        allContributionFieldsValid.set(areAllValid);
    };

    @FXML
    public void initialize() {
        ChangeListener<Object> calculateUniqueColors = createCalculateUniqueListener(
                ContributionField::getColor, DuplicateContributionField::duplicateColorProperty);
        ChangeListener<Object> calculateUniqueContributions = createCalculateUniqueListener(
                ContributionField::getContribution, DuplicateContributionField::duplicateContributionProperty);

        contributionFields.addListener(calculateUniqueColors);
        contributionFields.addListener(calculateUniqueContributions);
        contributionFields.addListener(calculateAllContributionFieldsValid);
        contributionFields.addListener((ListChangeListener.Change<? extends DuplicateContributionField> change) -> {
            while (change.next()) {
                change.getAddedSubList().forEach(addedCf -> {
                    addedCf.setColor(PREDEFINED_COLORS.stream()
                            .sequential()
                            .filter(
                                    predefColor -> contributionFields.stream()
                                            .map(ContributionField::getColor)
                                            .noneMatch(c -> c.equals(predefColor)))
                            .findFirst()
                            //CHECKSTYLE.OFF: MagicNumber - The range of rgb is defined from 0 to 255.
                            .orElse(Color.rgb(COLOR_RANDOM.nextInt(256),
                                    COLOR_RANDOM.nextInt(256), COLOR_RANDOM.nextInt(256))));
                    //CHECKSTYLE.OFF: MagicNumber
                    contributionFieldsBox.getChildren().add(createContributionRow(addedCf));
                    addedCf.colorProperty().addListener(calculateUniqueColors);
                    addedCf.contributionProperty().addListener(calculateUniqueContributions);
                    addedCf.validProperty().addListener(calculateAllContributionFieldsValid);
                });
                change.getRemoved().forEach(removedCf -> {
                    List<HBox> hboxes = contributionFieldsBox.getChildren().stream()
                            //If working as expected there should only be objects of the class HBox
                            .filter(node -> node instanceof HBox)
                            .map(node -> (HBox) node)
                            .filter(hbox -> hbox.getChildren().contains(removedCf))
                            .collect(Collectors.toList());
                    if (hboxes.isEmpty()) {
                        LOGGER.log(Level.WARNING, "Could not remove row containing the removed ContributionField.");
                    } else {
                        if (hboxes.size() > 1) { //NOPMD - Check for ambiguous choices.
                            LOGGER.log(Level.WARNING, "Found multiple rows containing the removed ContributionField.\n"
                                    + "Only first one got removed.");
                        }
                        contributionFieldsBox.getChildren().remove(hboxes.get(0));
                    }
                });
            }
        });
        bindValidProperty(allContributionFieldsValid);
        addContributionField();
    }

    private <T> ChangeListener<Object> createCalculateUniqueListener(Function<DuplicateContributionField, T> toCheck,
            Function<DuplicateContributionField, BooleanProperty> toStore) {
        return (obs, oldVal, newVal) -> {
            //Check for duplicates
            Set<T> uniqueElements = new HashSet<>();
            Set<T> duplicateElements = contributionFields.stream()
                    .map(toCheck)
                    .filter(element -> !uniqueElements.add(element))
                    .collect(Collectors.toSet());
            contributionFields.stream()
                    .forEach(cf -> toStore.apply(cf).set(duplicateElements.contains(toCheck.apply(cf))));
        };
    }

    /**
     * Adds the given listener to the property holding all contribution fields.
     *
     * @param listener The listener to be called when the property holding all contribution fields changes.
     */
    protected void addListenerToContributionFields(ListChangeListener<ContributionField> listener) {
        contributionFields.addListener(listener);
    }

    private HBox createContributionRow(DuplicateContributionField duplicateCf) {
        String remove = resources.getString("remove");
        Button removeButton = new Button(remove);
        removeButton.disableProperty().bind(contributionFields.sizeProperty().lessThanOrEqualTo(1));
        removeButton.setOnAction(aevt -> removeContributionField(duplicateCf));
        return new HBox(duplicateCf, removeButton);
    }

    @FXML
    private void addContributionField() {
        contributionFields.add(new DuplicateContributionField());
    }

    @FXML
    private void removeContributionField(DuplicateContributionField contributionField) {
        contributionFields.remove(contributionField);
    }

    /**
     * Returns the currently inserted contributions. Returns {@link Optional#empty()} if the user didn't confirm the
     * contribution yet or the input is invalid.
     *
     * @return The currently inserted contributions and their colors.
     */
    @Override
    protected Optional<BiMap<Double, Color>> calculateResult() {
        BiMap<Double, Color> contributions = HashBiMap.create(contributionFields.getSize());
        contributionFields.forEach(cf -> {
            cf.getContribution().ifPresent(contribution -> {
                contributions.put(contribution, cf.getColor());
            });
        });
        return Optional.of(contributions);
    }

    /**
     * The same as {@link ContributionField} but with an additional property holding whether it has duplicate value or
     * color.
     */
    private static class DuplicateContributionField extends ContributionField {

        private static final PseudoClass DUPLICATE_PSEUDO_CLASS = PseudoClass.getPseudoClass("duplicate");
        private final BooleanProperty duplicateColor = new SimpleBooleanProperty(false) {

            //TODO Try to avoid.
            private final Optional<ColorPicker> colorPicker = getChildren().stream()
                    .filter(child -> child instanceof ColorPicker)
                    .map(child -> (ColorPicker) child)
                    .findAny();

            @Override
            protected void invalidated() {
                colorPicker.ifPresent(cp -> cp.pseudoClassStateChanged(DUPLICATE_PSEUDO_CLASS, get()));
            }
        };
        private final BooleanProperty duplicateContribution = new SimpleBooleanProperty(false) {

            //TODO Try to avoid.
            private final Optional<CheckedDoubleSpinner> spinner = getChildren().stream()
                    .filter(child -> child instanceof CheckedDoubleSpinner)
                    .map(child -> (CheckedDoubleSpinner) child)
                    .findAny();

            @Override
            protected void invalidated() {
                spinner.ifPresent(cs -> cs.pseudoClassStateChanged(DUPLICATE_PSEUDO_CLASS, get()));
            }
        };

        /**
         * Creates a {@link DuplicateContributionField} where neither the duplicated color nor the duplicated
         * contribution flag is set.
         */
        DuplicateContributionField() {
            super(0, Double.MAX_VALUE, 10, 1, true);
            initProperties();
        }

        private void initProperties() {
            addReport(new ReportEntry("duplicateColor", ReportType.ERROR, duplicateColor));
            addReport(new ReportEntry("duplicateContribution", ReportType.ERROR, duplicateContribution));
        }

        /**
         * Returns the property holding the duplicated color flag. The duplicated color flag can be set to specify that
         * this control represents a color which another control also represents.
         *
         * @return The property holding the duplicated color flag.
         */
        public BooleanProperty duplicateColorProperty() {
            return duplicateColor;
        }

        /**
         * Checks whether the duplicated color flag is set.
         *
         * @return {@code true} only if the duplicated color flag is set.
         * @see #duplicateColorProperty()
         */
        public boolean isDuplicateColor() {
            return duplicateColorProperty().get();
        }

        /**
         * Changes the duplicate color flag.
         *
         * @param isDuplicate {@code true} if the duplicate color flag has to be set.
         * @see #duplicateColorProperty()
         */
        public void setDuplicateColor(boolean isDuplicate) {
            duplicateColorProperty().set(isDuplicate);
        }

        /**
         * Returns the property holding the duplicated contribution flag. The duplicated contribution flag can be set to
         * specify that this control contains a contribution another control also contains.
         *
         * @return
         */
        public BooleanProperty duplicateContributionProperty() {
            return duplicateContribution;
        }

        /**
         * Checks whether the duplicated contribution flag is set.
         *
         * @return {@code true} only if the duplicated contribution flag is set.
         * @see #duplicateContributionProperty()
         */
        public boolean isDuplicateContribution() {
            return duplicateContributionProperty().get();
        }

        /**
         * Changes the duplicated contribition flag.
         *
         * @param isDuplicate {@code true} only if the duplicated contribution flag has to be set.
         * @see #duplicateContributionProperty()
         */
        public void setDuplicateContribution(boolean isDuplicate) {
            duplicateContributionProperty().set(isDuplicate);
        }
    }
}
