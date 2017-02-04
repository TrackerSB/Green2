/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package bayern.steinbrecher.green2.elements;

import bayern.steinbrecher.green2.Controller;
import bayern.steinbrecher.green2.View;
import bayern.steinbrecher.green2.data.DataProvider;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.stream.IntStream;

/**
 * Represents a screen showing a fancy wait symbol.
 *
 * @author Stefan Huber
 */
public class WaitScreen extends View<Controller> {

    private static final int CORNERCOUNT = 6;
    private static final double ANGLE = 360.0 / CORNERCOUNT;
    private static final Rotate ROTATION = new Rotate(ANGLE);
    private static final Point2D[] DIRECTION_VECTORS = new Point2D[CORNERCOUNT];
    private static final int RADIUS = 15;
    private static final int DIAMETER = 2 * RADIUS;
    private static final int VERTICAL_COUNT = 5;
    private static final int HORIZONTAL_COUNT = 9;
    private static final Duration DURATION_ANIMATION = Duration.millis(600);
    private static final Duration DURATION_ANIMATION_HALF = DURATION_ANIMATION.divide(2);
    private static final Duration DURATION_PAUSE = Duration.seconds(3);
    private static final int DELAY = 200;
    private static final double START_OPACITY = 0.5;
    private static final double CENTER_OPACITY = 1;
    private final ParallelTransition overallTransition = new ParallelTransition();

    static {
        DIRECTION_VECTORS[0] = new Point2D(0, 1);
        for (int i = 1; i < CORNERCOUNT; i++) {
            DIRECTION_VECTORS[i] = ROTATION.transform(DIRECTION_VECTORS[i - 1]);
        }
    }

    /**
     * Default constructor.
     */
    public WaitScreen() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) {
        this.stage = stage;

        Pane root = new Pane();
        root.setStyle("-fx-background-color: rgba(0,0,0,0)");

        IntStream.range(0, VERTICAL_COUNT).parallel().forEach(row -> {
            int shorten = (row + 1) % 2;
            double yCoo = row * DIAMETER + RADIUS
                    - 1.5 * row * RADIUS / CORNERCOUNT;
            IntStream.range(0, HORIZONTAL_COUNT - shorten)
                    .parallel()
                    .forEach(column -> {
                        double xCoo = column * DIAMETER + (shorten + 1) * RADIUS;
                        Polygon p = createHexagon(new Point2D(xCoo, yCoo), RADIUS);
                        p.setOpacity(START_OPACITY);
                        p.setFill(Color.FORESTGREEN);

                        RotateTransition rt1 = new RotateTransition(DURATION_ANIMATION, p);
                        rt1.setByAngle(ANGLE / 2);
                        FadeTransition ft11 = new FadeTransition(DURATION_ANIMATION_HALF, p);
                        ft11.setFromValue(START_OPACITY);
                        ft11.setToValue(CENTER_OPACITY);
                        FadeTransition ft12 = new FadeTransition(DURATION_ANIMATION_HALF, p);
                        ft12.setFromValue(CENTER_OPACITY);
                        ft12.setToValue(START_OPACITY);
                        SequentialTransition st1 = new SequentialTransition(ft11, ft12);
                        ParallelTransition plt1 = new ParallelTransition(rt1, st1);

                        PauseTransition pt1 = new PauseTransition(DURATION_PAUSE);

                        RotateTransition rt2 = new RotateTransition(DURATION_ANIMATION, p);
                        rt2.setByAngle(ANGLE / 2);
                        FadeTransition ft21 = new FadeTransition(DURATION_ANIMATION_HALF, p);
                        ft21.setFromValue(START_OPACITY);
                        ft21.setToValue(CENTER_OPACITY);
                        FadeTransition ft22 = new FadeTransition(DURATION_ANIMATION_HALF, p);
                        ft22.setFromValue(START_OPACITY);
                        ft22.setToValue(CENTER_OPACITY);
                        SequentialTransition st2 = new SequentialTransition(ft11, ft12);
                        ParallelTransition plt2 = new ParallelTransition(rt2, st2);

                        PauseTransition pt2 = new PauseTransition(DURATION_PAUSE);

                        SequentialTransition sequence = new SequentialTransition(plt1, pt1, plt2, pt2);
                        sequence.setDelay(Duration.millis((column + row) * DELAY));
                        sequence.setCycleCount(Animation.INDEFINITE);

                        synchronized (root) {
                            root.getChildren().add(p);
                        }
                        synchronized (overallTransition) {
                            overallTransition.getChildren().add(sequence);
                        }
                    });
        });

        overallTransition.play();

        Scene scene = new Scene(root, HORIZONTAL_COUNT * DIAMETER, VERTICAL_COUNT * DIAMETER);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle(DataProvider.getResourceValue("waitAMoment"));
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.ImageSet.LOGO.get());
    }

    /**
     * Closes this WaitScreen if still open.
     */
    public void close() {
        checkStage();
        stage.close();
    }

    /**
     * Shows this WaitScreen if not yet shown.
     */
    public void show() {
        checkStage();
        stage.show();
    }

    /**
     * Creates a hexagon.
     *
     * @param center The center of the hexagon.
     * @param radius The radius of the circle the hexagon is within.
     * @return The polygon representing the hexagon.
     */
    public static Polygon createHexagon(Point2D center, double radius) {
        double[] coo = new double[DIRECTION_VECTORS.length * 2];
        for (int i = 0; i < DIRECTION_VECTORS.length; i++) {
            Point2D vector = center.add(DIRECTION_VECTORS[i].multiply(radius));
            coo[2 * i] = vector.getX();
            coo[2 * i + 1] = vector.getY();
        }
        return new Polygon(coo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanBinding wouldShowBinding() {
        throw new UnsupportedOperationException("WaitScreen does not use showOnceAndWait()");
    }
}
