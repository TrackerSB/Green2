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
package bayern.steinbrecher.green2.elements;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.utility.BindingUtility;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.beans.Observable;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Allows to display a summary of given errors and warnings.
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
                            reportsBox.getChildren().add(reportLabel);
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
     * If a report with given message already exists its counter is increased and its type is set to {@code type}.
     * Otherwise a new report with the given message and type is added.
     *
     * @param message The message of the report to increase its counter to add.
     * @param type The type the new report has or the existing report has to be set to.
     */
    public void increaseReportEntry(String message, ReportType type) {
        findReportEntry(message).ifPresentOrElse(entry -> {
            entry.setReportType(type);
            entry.increaseOccurrences();
        }, () -> reportEntries.add(new ReportEntry(message, type)));
    }

    /**
     * Decreases the counter of the report having this message. If the new number of occurrences is less than one the
     * report is removed.
     *
     * @param message The message of the report whose counter has to be decreased.
     */
    public void decreaseReportEntry(String message) {
        ReportEntry entry = findReportEntry(message).orElseThrow();
        entry.decreaseOccurrences();
        if (entry.getOccurrences() < 1) {
            reportEntries.remove(entry);
        }
    }

    /**
     * Removes the report having the given message no matter how many occurrences currently are registered.
     *
     * @param message The message of the report to remove.
     */
    public void removeReportEntry(String message) {
        ReportEntry entry = findReportEntry(message).orElseThrow();
        reportEntries.remove(entry);
    }

    /**
     * Represents classifications of report entries.
     */
    public static enum ReportType {
        /**
         * Marks a report entry as errors.
         */
        ERROR("error"),
        /**
         * Marks a report entry as additional information.
         */
        INFO("info"),
        /**
         * Marks not yet classified report entries.
         */
        UNDEFINED(null),
        /**
         * Marks a report entry as warning.
         */
        WARNING("warning");

        private final String graphic;

        private ReportType(String graphic) {
            this.graphic = graphic;
        }

        public ImageView getGraphic() {
            ImageView view;
            if (graphic == null) {
                view = null;
            } else {
                view = EnvironmentHandler.ImageSet.valueOf(graphic).getAsImageView();
            }
            return view;
        }
    }

    private static class ReportEntry {

        private final StringProperty message = new SimpleStringProperty(this, "message");
        private final ObjectProperty<ReportType> reportType = new SimpleObjectProperty<>(this, "reportType");
        private final IntegerProperty occurrences = new SimpleIntegerProperty(this, "occurrences", 1);

        public ReportEntry(String message, ReportType type) {
            setMessage(message);
            setReportType(type);
        }

        public StringProperty messageProperty() {
            return message;
        }

        public String getMessage() {
            return messageProperty().get();
        }

        public void setMessage(String message) {
            messageProperty().set(message);
        }

        public ObjectProperty<ReportType> reportTypeProperty() {
            return reportType;
        }

        public ReportType getReportType() {
            return reportTypeProperty().get();
        }

        public void setReportType(ReportType reportType) {
            reportTypeProperty().set(reportType);
        }

        public IntegerProperty occurrencesProperty() {
            return occurrences;
        }

        public int getOccurrences() {
            return occurrencesProperty().get();
        }

        public void setOccurrences(int occurrenes) {
            occurrencesProperty().set(occurrenes);
        }

        public void increaseOccurrences() {
            occurrencesProperty().set(occurrencesProperty().get() + 1);
        }

        public void decreaseOccurrences() {
            occurrencesProperty().set(occurrencesProperty().get() - 1);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ReportEntry) {
                ReportEntry entry = (ReportEntry) obj;
                return getMessage().equalsIgnoreCase(entry.getMessage());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            //NOTE This is the default implementation of NetBeans
            int hash = 3;
            hash = 97 * hash + Objects.hashCode(this.message);
            hash = 97 * hash + Objects.hashCode(this.reportType);
            hash = 97 * hash + Objects.hashCode(this.occurrences);
            return hash;
        }
    }
}
