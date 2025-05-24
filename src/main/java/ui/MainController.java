package ui;

import javax.sound.sampled.LineUnavailableException;

import core.AudioPlayer;
import core.TTS;
import core.YoutubeAudioDownloader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController {
	
	private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private TextField ttsText, filePath, ytText;

    @FXML
    private Button btnEnviarTTS, btnSeleccionarArchivo, btnYT;

    @FXML
    private Button closeBtn, minimizeBtn;
    
    @FXML
    private AnchorPane topBar;


    
    @FXML
    private void onSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona un archivo MP3");

        // Filtrar solo archivos .mp3
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos MP3 (*.mp3)", "*.mp3");
        fileChooser.getExtensionFilters().add(extFilter);

        Stage stage = (Stage) filePath.getScene().getWindow();
        var selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            filePath.setText(selectedFile.getAbsolutePath());
        }
    }
    
    @FXML
    private void sendTTS() {
        String texto = ttsText.getText();
        // Aquí llamas al código que haga el TTS con "texto"
        System.out.println("Enviar TTS: " + texto);
        try {
			AudioPlayer playerTTS = new AudioPlayer("CABLE Input");
			playerTTS.playMp3(TTS.fetchAudioToFile(texto));
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @FXML
    private void sendYTURL() {
        String texto = ytText.getText();
        System.out.println("Enviar YT: " + texto);
        try {
			AudioPlayer playerTTS = new AudioPlayer("CABLE Input");
			playerTTS.playMp3(YoutubeAudioDownloader.downloadAudioSegment(texto, 0, 300));
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @FXML
    private void close() {
    	Platform.exit();
    }
    @FXML
    private void minimize() {
    	Stage stage = (Stage) filePath.getScene().getWindow();
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
}
