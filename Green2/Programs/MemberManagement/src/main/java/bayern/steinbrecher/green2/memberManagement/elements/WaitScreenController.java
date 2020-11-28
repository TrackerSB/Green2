package bayern.steinbrecher.green2.memberManagement.elements;

import bayern.steinbrecher.wizard.StandaloneWizardPageController;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.Optional;
import java.util.stream.IntStream;

/**
 * @author Stefan Huber
 * @since 2u14
 */
public class WaitScreenController extends StandaloneWizardPageController<Optional<Void>> {
    private static final int CORNER_COUNT = 6;
    private static final double ANGLE = 360.0 / CORNER_COUNT;
    private static final Rotate ROTATION = new Rotate(ANGLE);
    private static final Point2D[] DIRECTION_VECTORS = new Point2D[CORNER_COUNT];
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

    @FXML
    private Pane root;

    static {
        DIRECTION_VECTORS[0] = new Point2D(0, 1);
        for (int i = 1; i < CORNER_COUNT; i++) {
            DIRECTION_VECTORS[i] = ROTATION.transform(DIRECTION_VECTORS[i - 1]);
        }
    }

    @FXML
    private void initialize() {
        IntStream.range(0, VERTICAL_COUNT).parallel().forEach(row -> {
            int shorten = (row + 1) % 2;
            //CHECKSTYLE.OFF: MagicNumber - The factor 1.5 is needed for correct the alignment of the displayed objects.
            double yCoo = row * DIAMETER + RADIUS
                    - 1.5 * row * RADIUS / CORNER_COUNT;
            //CHECKSTYLE.ON: MagicNumber
            IntStream.range(0, HORIZONTAL_COUNT - shorten).parallel().forEach(column -> {
                double xCoo = column * DIAMETER + (shorten + 1) * RADIUS;
                Polygon polygon = createPolygon(new Point2D(xCoo, yCoo));
                polygon.setOpacity(START_OPACITY);
                polygon.setFill(Color.FORESTGREEN);

                Animation wave1 = createPartialAnimation(polygon);
                PauseTransition pause1 = new PauseTransition(DURATION_PAUSE);
                Animation wave2 = createPartialAnimation(polygon);
                PauseTransition pause2 = new PauseTransition(DURATION_PAUSE);
                SequentialTransition sequence = new SequentialTransition(wave1, pause1, wave2, pause2);
                sequence.setDelay(Duration.millis((column + row) * DELAY));
                sequence.setCycleCount(Animation.INDEFINITE);

                synchronized (root) {
                    root.getChildren().add(polygon);
                }
                synchronized (overallTransition) {
                    overallTransition.getChildren().add(sequence);
                }
            });
        });

        stageProperty().addListener((stageObs, oldStage, newStage) -> {
            if (newStage != null) {
                newStage.showingProperty().addListener((obs, wasShowing, isShowing) -> {
                    if (isShowing) {
                        overallTransition.play();
                    } else {
                        overallTransition.pause();
                    }
                });
            }
        });
    }

    private static Polygon createPolygon(Point2D center) {
        double[] coo = new double[DIRECTION_VECTORS.length * 2];
        for (int i = 0; i < DIRECTION_VECTORS.length; i++) {
            Point2D vector = center.add(DIRECTION_VECTORS[i].multiply(RADIUS));
            coo[2 * i] = vector.getX();
            coo[2 * i + 1] = vector.getY();
        }
        return new Polygon(coo);
    }

    private static Animation createPartialAnimation(Polygon polygon) {
        RotateTransition rotate = new RotateTransition(DURATION_ANIMATION, polygon);
        rotate.setByAngle(ANGLE / 2);
        FadeTransition fadeIn = new FadeTransition(DURATION_ANIMATION_HALF, polygon);
        fadeIn.setFromValue(START_OPACITY);
        fadeIn.setToValue(CENTER_OPACITY);
        FadeTransition fadeOut = new FadeTransition(DURATION_ANIMATION_HALF, polygon);
        fadeOut.setFromValue(CENTER_OPACITY);
        fadeOut.setToValue(START_OPACITY);
        SequentialTransition fadeSequence = new SequentialTransition(fadeIn, fadeOut);
        return new ParallelTransition(rotate, fadeSequence);
    }
}
