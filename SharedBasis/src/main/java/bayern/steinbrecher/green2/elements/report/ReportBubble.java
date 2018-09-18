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

import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * Represents a bubble like popup showing attached and triggered reports of a control.It is meant for being used for
 * inline validations.
 *
 * @author Stefan Huber
 * @param <C> The type of the reporting {@link Node} where to attach a {@link ReportBubble} to.
 * @since 2u14
 */
public final class ReportBubble<C extends Node & Reportable> {

    /**
     * The map has the following structure: ReportType --> (background color, font color). The colors represent CSS
     * values.
     */
    private static final Map<ReportType, Pair<String, String>> COLOR_SCHEMES = Map.of(
            ReportType.ERROR, new Pair<>("#ff4d4d", "white"),
            ReportType.WARNING, new Pair<>("#ffe680", "black"),
            ReportType.INFO, new Pair<>("#80bfff", "black"),
            ReportType.UNDEFINED, new Pair<>("#d9d9d9", "black")
    );
    private final ListProperty<ReportEntry> triggeredReports = new SimpleListProperty<>(this, "triggeredReports");
    private final ReadOnlyStringWrapper reportsMessage = new ReadOnlyStringWrapper(this, "reportsMessage");
    private final Tooltip bubble = new Tooltip();

    public ReportBubble(C reportable) {
        bubble.textProperty()
                .bind(reportsMessage);

        reportable.getReports()
                .addListener((ListChangeListener.Change<? extends ReportEntry> change) -> {
                    triggeredReports.set(FXCollections.observableArrayList(change.getList()
                            .stream()
                            .filter(r -> r.getReportTrigger().get())
                            .collect(Collectors.toList())));
                });
        triggeredReports.addListener((obs, oldVal, newVal) -> {
            StringJoiner reportBuilder = new StringJoiner("\n");
            newVal.stream()
                    .forEach(reportEntry -> {
                        reportBuilder.add(reportEntry.getMessage());
                    });
            reportsMessage.set(reportBuilder.toString());
        });
        triggeredReports.emptyProperty()
                .not()
                .and(reportable.validProperty().not())
                .and(reportable.focusedProperty())
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        ReportType bubbleType = triggeredReports
                                .stream()
                                .map(ReportEntry::getType)
                                .distinct()
                                .max((typeA, typeB) -> typeA.compareTo(typeB))
                                .orElse(ReportType.UNDEFINED);
                        Pair<String, String> scheme = COLOR_SCHEMES.get(bubbleType);
                        assert scheme != null : "There is no scheme defined for ReportType " + bubbleType;

                        bubble.setStyle("-fx-background-color: " + scheme.getKey() + ";"
                                + "-fx-text-fill: " + scheme.getValue());

                        Tooltip.install(reportable, bubble);
                    } else {
                        Tooltip.uninstall(reportable, bubble);
                    }
                });
    }
}
