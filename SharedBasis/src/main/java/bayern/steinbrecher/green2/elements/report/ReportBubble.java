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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.collections.ListChangeListener;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.util.Pair;

/**
 * Represents a bubble like popup showing attached and triggered reports of a control. It is meant for being used for
 * inline validations.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public class ReportBubble {

    private static final double BUBBLE_ARC = 30;
    private static final double HALF_BUBBLE_ARC = BUBBLE_ARC / 2;
    private static final double MAX_BUBBLE_WIDTH = 400;
    private static final String FONT_URL = ReportBubble.class.getResource("/lato/Lato-Light.ttf")
            .toExternalForm();
    private static final double FONT_SIZE = 14;
    /**
     * The map has the following structure: ReportType --> (background color, font color)
     */
    private static final Map<ReportType, Pair<Color, Color>> COLOR_SCHEMES = Map.of(
            ReportType.ERROR, new Pair<>(Color.web("#ff4d4d"), Color.WHITE),
            ReportType.WARNING, new Pair<>(Color.web("#ffe680"), Color.BLACK),
            ReportType.INFO, new Pair<>(Color.web("#80bfff"), Color.BLACK),
            ReportType.UNDEFINED, new Pair<>(Color.web("#d9d9d9"), Color.BLACK)
    );
    private final Popup bubble = new Popup();

    public ReportBubble(Reportable reportable) {
        Canvas bubbleCanvas = new Canvas();

        bubble.setOpacity(0);
        bubble.setAutoFix(true);
        bubble.getContent()
                .add(bubbleCanvas);

        reportable.getReports()
                .addListener((ListChangeListener.Change<? extends ReportEntry> change) -> {
                    List<? extends ReportEntry> triggeredReports = change.getList()
                            .stream()
                            .filter(report -> report.getReportTrigger().get())
                            .collect(Collectors.toList());
                    if (triggeredReports.isEmpty()) {
                        bubble.setAutoHide(false);
                        bubble.hide();
                    } else {
                        bubble.setAutoHide(true);
                        ReportType bubbleType = triggeredReports
                                .stream()
                                .map(ReportEntry::getType)
                                .distinct()
                                .max((typeA, typeB) -> typeA.compareTo(typeB))
                                .orElse(ReportType.UNDEFINED);
                        Pair<Color, Color> scheme = COLOR_SCHEMES.get(bubbleType);
                        assert scheme != null : "There is no scheme defined for ReportType " + bubbleType;

                        GraphicsContext bubbleContext = bubbleCanvas.getGraphicsContext2D();
                        bubbleContext.setFill(scheme.getKey());
                        bubbleContext.fillRoundRect(
                                0, 0, bubbleCanvas.getWidth(), bubbleCanvas.getHeight(), BUBBLE_ARC, BUBBLE_ARC);
                        bubbleContext.setFill(scheme.getValue());
                        bubbleContext.setTextBaseline(VPos.TOP);
                        bubbleContext.setFont(Font.loadFont(FONT_URL, FONT_SIZE));

                        int numInsertedReports = 0;
                        for (ReportEntry report : change.getList()) {
                            if (report.getReportTrigger().get()) {
                                double yOffset = HALF_BUBBLE_ARC + numInsertedReports * FONT_SIZE;
                                bubbleContext.fillText(
                                        report.getMessage(), HALF_BUBBLE_ARC, yOffset, MAX_BUBBLE_WIDTH);
                                numInsertedReports++;
                            }
                        }
                        bubbleCanvas.setWidth(MAX_BUBBLE_WIDTH);
                        bubbleCanvas.setHeight(BUBBLE_ARC + numInsertedReports * FONT_SIZE);
                    }
                });
    }

    public Popup getBubble() {
        return bubble;
    }
}
