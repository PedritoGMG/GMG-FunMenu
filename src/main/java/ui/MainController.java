package ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.sound.sampled.LineUnavailableException;

import core.Main;
import core.audio.AudioPlayer;
import core.audio.plugin.TTS;
import core.audio.plugin.YoutubeAudioDownloader;
import core.util.Toast;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainController implements Initializable{

    private Timeline currentAnimation = null;
    private String pendingPage = null;
	private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private Button closeBtn, minimizeBtn;
    
    @FXML
    private AnchorPane topBar;
    
    @FXML
    private StackPane stackPaneMain;


    @Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			pageMain(null);
			Toast.showIn(stackPaneMain, "Hola buenos dias", 8000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
    
    @FXML
    private void close() {
        Main.OnExit();
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void minimize() {
    	Stage stage = (Stage) topBar.getScene().getWindow();
    	stage.setIconified(true);
    }
    @FXML
    private void onMousePressedTopBar(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void onMouseDraggedTopBar(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }
    
    @FXML
    private void pageMain(ActionEvent actionEvent) throws IOException {
        loadPage("pageMain");
    }
    @FXML
    private void pagePlugins(ActionEvent actionEvent) throws IOException {
        loadPage("pagePlugins");
    }
    @FXML
    private void pageBindsTriggers(ActionEvent actionEvent) throws IOException {
        loadPage("pageBindsTriggers");
    }
    @FXML
    private void pageConsole(ActionEvent actionEvent) throws IOException {
        loadPage("pageConsole");
    }
    @FXML
    private void pageSettings(ActionEvent actionEvent) throws IOException {
        loadPage("pageSettings");
    }

    private void loadPage(String fxmlName) throws IOException {
        String currentShown = (String) stackPaneMain.getUserData();
        if (fxmlName.equals(currentShown) || fxmlName.equals(pendingPage)) return;

        pendingPage = fxmlName;

        stopCurrentAnimationAndClean();

        Parent newPage = new FXMLLoader(getClass().getResource("/ui/" + fxmlName + ".fxml")).load();
        prepareNodeForAnimation(newPage);

        Node currentPage = stackPaneMain.getChildren().isEmpty() ? null : stackPaneMain.getChildren().get(0);
        stackPaneMain.getChildren().add(newPage);

        Duration duration = Duration.millis(180);
        Timeline enter = createEnterTimeline(newPage, duration);

        enter.setOnFinished(e -> {
            if (!fxmlName.equals(pendingPage)) {
                currentAnimation = null;
                return;
            }
            if (currentPage != null) stackPaneMain.getChildren().remove(currentPage);
            newPage.setCache(false);
            if (currentPage != null) currentPage.setCache(false);

            stackPaneMain.setUserData(fxmlName);
            pendingPage = null;
            currentAnimation = null;
        });

        currentAnimation = enter;
        enter.play();
    }

    private void stopCurrentAnimationAndClean() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }

        while (stackPaneMain.getChildren().size() > 1) {
            stackPaneMain.getChildren().remove(0);
        }
        if (!stackPaneMain.getChildren().isEmpty()) {
            Node visible = stackPaneMain.getChildren().get(0);
            visible.setOpacity(1);
            visible.setTranslateX(0);
            visible.setScaleX(1);
            visible.setScaleY(1);
            visible.setCache(false);
        }
    }

    private void prepareNodeForAnimation(Parent node) {
        node.applyCss();
        node.layout();
        double width = stackPaneMain.getWidth();
        if (width <= 0 && stackPaneMain.getScene() != null) width = stackPaneMain.getScene().getWidth();
        if (width <= 0) width = 800;

        node.setTranslateX(width * 0.12);
        node.setOpacity(0);
        node.setCache(true);
        node.setCacheHint(CacheHint.SPEED);
    }

    private Timeline createEnterTimeline(Node node, Duration dur) {
        return new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(node.opacityProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(node.translateXProperty(), node.getTranslateX(), Interpolator.EASE_BOTH)
                ),
                new KeyFrame(dur,
                        new KeyValue(node.opacityProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(node.translateXProperty(), 0, Interpolator.EASE_BOTH)
                )
        );
    }
}
