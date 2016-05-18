package bayern.steinbrecher.gruen2.elements;

import bayern.steinbrecher.gruen2.data.DataProvider;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Represents a screen showing a fancy wait symbol.
 *
 * @author Stefan Huber
 */
public class WaitScreen {

    private static final int CORNERCOUNT = 6;
    private static final double ANGLE = 360.0 / CORNERCOUNT;
    private static final Rotate ROTATION = new Rotate(ANGLE);
    private static final Point2D[] DIRECTION_VECTORS = new Point2D[CORNERCOUNT];
    private static final int RADIUS = 15;
    private static final int DIAMETER = 2 * RADIUS;
    private static final int VERTICAL_COUNT = 5;
    private static final int HORIZONTAL_COUNT = 9;
    private static final Duration DURATION_ANIMATION = Duration.millis(600);
    private static final Duration DURATION_PAUSE = Duration.seconds(3);
    private static final int DELAY = 200;
    private static final int STAGE_MARGIN = 10;
    private Stage stage;

    static {
        DIRECTION_VECTORS[0] = new Point2D(0, -1);
        for (int i = 1; i < CORNERCOUNT; i++) {
            DIRECTION_VECTORS[i] = ROTATION.transform(DIRECTION_VECTORS[i - 1]);
        }
    }
    
    public static Stage showWaitScreen(Window owner){
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        
        Pane root = new Pane();

        for (double row = 0; row < VERTICAL_COUNT; row++) {
            int shorten = (int) Math.floor((row + 1) % 2);
            for (double column = 0; column < HORIZONTAL_COUNT - shorten; column++) {
                double xCoo = column * DIAMETER + (shorten + 1) * RADIUS
                        + STAGE_MARGIN;
                double yCoo = row * DIAMETER + RADIUS - row * RADIUS / 4
                        + STAGE_MARGIN;
                Polygon p = createPentagon(new Point2D(xCoo, yCoo), RADIUS);
                p.setStroke(Color.FORESTGREEN);
                p.setStrokeWidth(RADIUS / 4);
                p.setOpacity(0.5);
                p.setFill(Color.WHITE);

                RotateTransition rt1
                        = new RotateTransition(DURATION_ANIMATION, p);
                rt1.setByAngle(ANGLE / 2);
                FadeTransition ft11
                        = new FadeTransition(DURATION_ANIMATION.divide(2), p);
                ft11.setFromValue(0.5);
                ft11.setToValue(1);
                FadeTransition ft12
                        = new FadeTransition(DURATION_ANIMATION.divide(2), p);
                ft12.setFromValue(1);
                ft12.setToValue(0.5);
                SequentialTransition st1 = new SequentialTransition(ft11, ft12);
                ParallelTransition plt1 = new ParallelTransition(rt1, st1);

                PauseTransition pt1 = new PauseTransition(DURATION_PAUSE);

                RotateTransition rt2
                        = new RotateTransition(DURATION_ANIMATION, p);
                rt2.setByAngle(ANGLE / 2);
                FadeTransition ft21
                        = new FadeTransition(DURATION_ANIMATION.divide(2), p);
                ft21.setFromValue(0.5);
                ft21.setToValue(1);
                FadeTransition ft22
                        = new FadeTransition(DURATION_ANIMATION.divide(2), p);
                ft22.setFromValue(0.5);
                ft22.setToValue(1);
                SequentialTransition st2 = new SequentialTransition(ft11, ft12);
                ParallelTransition plt2 = new ParallelTransition(rt2, st2);

                PauseTransition pt2 = new PauseTransition(DURATION_PAUSE);

                SequentialTransition sequence
                        = new SequentialTransition(plt1, pt1, plt2, pt2);
                sequence.setDelay(Duration.millis((column + row) * DELAY));
                sequence.setCycleCount(Animation.INDEFINITE);
                sequence.play();

                root.getChildren().add(p);
            }
        }

        Scene scene = new Scene(root, (HORIZONTAL_COUNT + 1) * DIAMETER,
                (VERTICAL_COUNT + 1) * DIAMETER);
        scene.setFill(null);
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Einen Moment bitte");
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.getIcon());
        stage.show();
        
        return stage;
    }

    public static Polygon createPentagon(Point2D center, double radius) {
        double[] coo = new double[DIRECTION_VECTORS.length * 2];
        for (int i = 0; i < DIRECTION_VECTORS.length; i++) {
            Point2D vector = center.add(DIRECTION_VECTORS[i].multiply(radius));
            coo[2 * i] = vector.getX();
            coo[2 * i + 1] = vector.getY();
        }
        return new Polygon(coo);
    }
}
