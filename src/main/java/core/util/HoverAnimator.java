package core.util;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.util.Duration;

public final class HoverAnimator {

    public static void apply(Node node) {
        Duration duration = Duration.millis(150);

        ScaleTransition scaleUp = new ScaleTransition(duration, node);
        scaleUp.setToX(1.08);
        scaleUp.setToY(1.08);
        scaleUp.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition scaleDown = new ScaleTransition(duration, node);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.setInterpolator(Interpolator.EASE_BOTH);

        TranslateTransition liftUp = new TranslateTransition(duration, node);
        liftUp.setToY(-2);
        liftUp.setInterpolator(Interpolator.EASE_BOTH);

        TranslateTransition liftDown = new TranslateTransition(duration, node);
        liftDown.setToY(0);
        liftDown.setInterpolator(Interpolator.EASE_BOTH);

        node.setOnMouseEntered(e -> {
            scaleDown.stop();
            liftDown.stop();
            scaleUp.playFromStart();
            liftUp.playFromStart();
        });

        node.setOnMouseExited(e -> {
            scaleUp.stop();
            liftUp.stop();
            scaleDown.playFromStart();
            liftDown.playFromStart();
        });
    }

    public static void applySimpleHover(Node node) {
        double scaleAmount = 1.08;
        Duration duration = Duration.millis(150);

        ScaleTransition scaleUp = new ScaleTransition(duration, node);
        scaleUp.setToX(scaleAmount);
        scaleUp.setToY(scaleAmount);
        scaleUp.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition scaleDown = new ScaleTransition(duration, node);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.setInterpolator(Interpolator.EASE_BOTH);

        node.setOnMouseEntered(e -> {
            scaleDown.stop();
            scaleUp.playFromStart();
        });

        node.setOnMouseExited(e -> {
            scaleUp.stop();
            scaleDown.playFromStart();
        });
    }

    public static void applyThumbHover(Node thumb) {
        double scaleAmount = 1.15;
        Duration duration = Duration.millis(150);

        ScaleTransition scaleUp = new ScaleTransition(duration, thumb);
        scaleUp.setToX(scaleAmount);
        scaleUp.setToY(scaleAmount);
        scaleUp.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition scaleDown = new ScaleTransition(duration, thumb);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.setInterpolator(Interpolator.EASE_BOTH);

        thumb.setOnMouseEntered(e -> {
            scaleDown.stop();
            scaleUp.playFromStart();
        });
        thumb.setOnMouseExited(e -> {
            scaleUp.stop();
            scaleDown.playFromStart();
        });
    }

    public static void applyHoverToAllButtons(Scene scene) {
        if (scene == null) return;
        Platform.runLater(() -> {
            scene.getRoot().lookupAll(".button").forEach(HoverAnimator::apply);
        });
    }
}
