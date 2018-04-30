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
package bayern.steinbrecher.green2.elements.report;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.utility.BindingUtility;
import bayern.steinbrecher.green2.utility.ElementsUtility;
import bayern.steinbrecher.green2.utility.ReportSummaryBuilder;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.ListExpression;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * Allows to display a summary of given errors and warnings. For handling a {@link ReportSummary} a
 * {@link ReportSummaryBuilder} may be used.
 *
 * @author Stefan Huber
 */
public class ReportSummary extends TitledPane {

    //TODO Force uniqueness of messages.
    private final ListProperty<ReportEntry> reportEntries = new SimpleListProperty<>(this, "reportEntries",
            FXCollections.observableArrayList(
                    e -> new Observable[]{e.messageProperty(), e.occurrencesProperty(), e.reportTypeProperty()}));
    private final IntegerProperty numEntries = new SimpleIntegerProperty(this, "numEntries", 0);
    @FXML
    private VBox reportsBox;

    public ReportSummary() {
        loadFXML();

        StringExpression title = new SimpleStringProperty(EnvironmentHandler.getResourceValue("errorReport"))
                .concat(" (")
                .concat(numEntries)
                .concat(")");
        textProperty().bind(title);

        heightProperty().addListener((obs, oldVal, newVal) -> {
            double sceneHeight = getScene().getHeight();
            Window window = getScene().getWindow();
            double windowHeight = window.getHeight();
            Parent parent = getParent();
            while (parent != null && !(parent instanceof ScrollPane)) {
                parent = parent.getParent();
            }
            if (parent instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) parent;
                if (newVal.longValue() > oldVal.longValue()) {
                    scrollPane.setVvalue(scrollPane.getVmax());
                }
            } else {
                window.setHeight(sceneHeight);
            }
        });

        reportEntries.addListener((ListChangeListener.Change<? extends ReportEntry> change) -> {
            numEntries.bind(BindingUtility.reduceSum(reportEntries.stream().map(ReportEntry::occurrencesProperty)));

            while (change.next()) {
                change.getAddedSubList().stream()
                        .forEach(entry -> {
                            Label reportLabel = new Label();
                            StringExpression reportText = entry.messageProperty()
                                    .concat(" (")
                                    .concat(entry.occurrencesProperty())
                                    .concat(")");
                            reportLabel.textProperty().bind(reportText);
                            reportLabel.graphicProperty().bind(Bindings.createObjectBinding(
                                    () -> entry.getReportType().getGraphic(), entry.reportTypeProperty()));
                            for (ReportType type : ReportType.values()) {
                                ElementsUtility.addCssClassIf(
                                        reportLabel, entry.reportTypeProperty().isEqualTo(type), type.getCSSClass());
                            }
                            entry.occurrencesProperty().addListener((obs, oldVal, newVal) -> {
                                if (newVal.intValue() > 0) {
                                    if (!reportsBox.getChildren().contains(reportLabel)) {
                                        reportsBox.getChildren().add(reportLabel);
                                    }
                                } else {
                                    reportsBox.getChildren().remove(reportLabel);
                                }
                            });
                        });
                change.getRemoved().stream()
                        .map(ReportEntry::getMessage)
                        .map(this::getLabel)
                        .forEach(reportsBox.getChildren()::remove);
            }
        });
    }

    private void loadFXML() {
        FXMLLoader fxmlLoader = new FXMLLoader(
                ReportSummary.class.getResource("ReportSummary.fxml"), EnvironmentHandler.RESOURCE_BUNDLE);
        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException ex) {
            Logger.getLogger(ReportSummary.class.getName()).log(Level.SEVERE, "Could not load ReportSummary", ex);
        }
    }

    private Label getLabel(String reportMessage) {
        Pattern messagePattern = Pattern.compile(reportMessage + " \\(\\d+\\)");
        return reportsBox.getChildren().stream()
                //All children should be Labels
                .filter(child -> child instanceof Label)
                .map(child -> (Label) child)
                .filter(label -> messagePattern.matcher(label.getText()).matches())
                .findAny()
                //If it is not found an implementation error exists
                //NOTE Whether there are multiple results is not checked
                .get();
    }

    private Optional<ReportEntry> findReportEntry(String message) {
        return reportEntries.stream()
                .filter(entry -> entry.getMessage().equalsIgnoreCase(message))
                //There should be maximum one
                .findAny();
    }

    /**
     * If a report with given message already exists the validation is added and its type is set to
     * {@code type}.Otherwise a new report with the given message, type and first validation is added.
     *
     * @param message The message of the report to increase its counter to add.
     * @param type The type the new report has or the existing report has to be set to.
     * @param validation The initial expression checked whether the message occurrs.
     */
    public void addReportEntry(String message, ReportType type, BooleanExpression validation) {
        findReportEntry(message).ifPresentOrElse(entry -> {
            entry.addReportValidation(validation);
        }, () -> {
            ReportEntry entry = new ReportEntry(message, type);
            entry.addReportValidation(validation);
            reportEntries.add(entry);
        });
    }

    /**
     * Removes a report having the given message and all its validations.
     *
     * @param message The message of the report to remove.
     * @return {@code true} only if the report entry was removed.
     * @see ListExpression#remove(java.lang.Object)
     */
    public boolean removeReportEntry(String message) {
        ReportEntry entry = findReportEntry(message).orElseThrow();
        return reportEntries.remove(entry);
    }

    /**
     * Removes the given validation from the report having the given message. If no validations are left at the report
     * the report itself is removed.
     *
     * @param message The message of the report to remove from.
     * @param validation The validation to remove.
     * @return {@code true} only if the validation was removed.
     * @see ListExpression#remove(java.lang.Object)
     */
    public boolean removeReportValidation(String message, BooleanExpression validation) {
        ReportEntry entry = findReportEntry(message).orElseThrow();
        boolean validationRemoved = entry.removeReportValidation(validation);
        if (validationRemoved && entry.reportValidationsSizeProperty().get() < 1) {
            removeReportEntry(message);
        }
        return validationRemoved;
    }
}
