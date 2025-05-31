package core;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Toast {

    public static void showIn(Pane root, String message, int durationMillis) {
        showToast(root, message, durationMillis, 280);
    }

    public static void showIn(Stage stage, String message, int durationMillis) {
        Scene scene = stage.getScene();
        if (scene == null) return;

        if (!(scene.getRoot() instanceof StackPane)) {
            StackPane newRoot = new StackPane();
            newRoot.getChildren().add(scene.getRoot());
            scene.setRoot(newRoot);
        }

        StackPane root = (StackPane) scene.getRoot();

        showToast(root, message, durationMillis, 50);
    }

    private static void showToast(Pane root, String message, int durationMillis, double translateY) {
        Label toastLabel = new Label(message);
        toastLabel.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.75); " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10px 20px; " +
            "-fx-background-radius: 10px; " +
            "-fx-font-size: 14px;"
        );
        toastLabel.setOpacity(0);

        StackPane toastContainer = new StackPane(toastLabel);
        toastContainer.setMouseTransparent(true);
        toastContainer.setPickOnBounds(false);
        toastContainer.setTranslateY(translateY);
        StackPane.setAlignment(toastContainer, Pos.BOTTOM_CENTER);

        root.getChildren().add(toastContainer);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), toastLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        PauseTransition pause = new PauseTransition(Duration.millis(durationMillis));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), toastLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> root.getChildren().remove(toastContainer));

        new SequentialTransition(fadeIn, pause, fadeOut).play();
    }
}
