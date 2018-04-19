package paneswipetest;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Class for testing how to "swipe" from one pane to another.
 *
 * @author Stefan Huber
 */
public class PaneSwipeTest extends Application {

    private static final Duration DURATION = Duration.seconds(1);

    @Override
    public void start(Stage primaryStage) {
        Pane a = new Pane(new Label("Pane A"));
        a.setMinSize(100, 100);
        a.setStyle("-fx-background-color: red");
        VBox.setVgrow(a, Priority.ALWAYS);

        Pane b = new Pane(new Label("Pane B"));
        b.setMinSize(100, 100);
        b.setStyle("-fx-background-color: blue");
        b.setOpacity(0);
        VBox.setVgrow(b, Priority.ALWAYS);

        Pane paneView = new StackPane(a, b);

        Button fade = new Button("Fade");
        fade.setOnAction(aevt -> {
            ObservableList<Node> children = paneView.getChildren();
            FadeTransition ftFrom;
            FadeTransition ftTo;
            if (a.getOpacity() >= 1) {
                ftFrom = new FadeTransition(DURATION, a);
                ftTo = new FadeTransition(DURATION, b);
            } else {
                ftFrom = new FadeTransition(DURATION, b);
                ftTo = new FadeTransition(DURATION, a);
            }
            ftFrom.setFromValue(1);
            ftFrom.setToValue(0);
            ftTo.setFromValue(0);
            ftTo.setToValue(1);
            new ParallelTransition(ftFrom, ftTo).playFromStart();
        });

        Button swipe = new Button("Swipe");
        swipe.setOnAction(aevt -> {
            //use getLayoutBounds() or getBoundsInLocal()?
            double halfParentWidth = a.getParent().getLayoutBounds().getWidth() / 2;
            double halfParentHeight = a.getParent().getLayoutBounds().getHeight() / 2;
            boolean reverse = false;
            double xRightOuter = 3 * halfParentWidth;
            double xLeftOuter = -halfParentWidth;

            MoveTo initialMoveIn = new MoveTo(reverse ? xRightOuter : xLeftOuter, halfParentHeight);
            HLineTo hlineIn = new HLineTo(halfParentWidth);
            Path pathIn = new Path(initialMoveIn, hlineIn);
            PathTransition ptIn = new PathTransition(DURATION, pathIn);

            MoveTo initialMoveOut = new MoveTo(halfParentWidth, halfParentHeight);
            HLineTo hlineOut = new HLineTo(reverse ? xLeftOuter : xRightOuter);
            Path pathOut = new Path(initialMoveOut, hlineOut);
            PathTransition ptOut = new PathTransition(DURATION, pathOut);

            Transition transition = new ParallelTransition(ptIn, ptOut);
            if (a.getOpacity() >= 1) {
                b.setOpacity(1);
                ptIn.setNode(b);
                ptOut.setNode(a);
                transition.setOnFinished(aevt2 -> a.setOpacity(0));
            } else {
                a.setOpacity(1);
                ptIn.setNode(a);
                ptOut.setNode(b);
                transition.setOnFinished(aevt2 -> b.setOpacity(0));
            }
            transition.playFromStart();
        });

        HBox controls = new HBox(fade, swipe);
        VBox root = new VBox(10, paneView, controls);

        Scene scene = new Scene(root, 400, 200);

        primaryStage.setTitle("PaneTransitionTest");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
