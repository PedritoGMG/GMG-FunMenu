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
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
        if (!stackPaneMain.getChildren().isEmpty()) {
            Node current = stackPaneMain.getChildren().get(0);
            if (fxmlName.equals(current.getUserData()))
                return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/" + fxmlName + ".fxml"));
        Parent page = loader.load();
        page.setUserData(fxmlName);

        stackPaneMain.getChildren().setAll(page);
    }
	
}
