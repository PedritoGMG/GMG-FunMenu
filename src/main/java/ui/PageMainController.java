package ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.sound.sampled.LineUnavailableException;

import core.AudioPlayer;
import core.Main;
import core.TTS;
import core.YoutubeAudioDownloader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class PageMainController implements Initializable{
	
    @FXML
    private TextField ttsText, filePath, ytText;

    @FXML
    private Button btnEnviarTTS, btnSeleccionarArchivo, btnYT;
    
    @FXML
    private Slider sliderTTS, sliderMusic, sliderAudio;
	
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSlider(sliderTTS, Main.playerTTS.getAudioPlayer());
        setupSlider(sliderMusic, Main.playerMusic.getAudioPlayer());
        setupSlider(sliderAudio, Main.playerAudio);
        
        try {
			Main.playerMusic.getAudioPlayer().playMp3(YoutubeAudioDownloader.downloadAudioSegment("https://www.youtube.com/watch?v=OfON90bvRnI", 0, 360));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
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
    
    private void setupSlider(Slider slider, AudioPlayer audioPlayer) {
        slider.setMin(0);
        slider.setMax(1);
        slider.setValue(audioPlayer.getVolume());
        
        Platform.runLater(() -> updateSliderTrackStyle(slider));

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
        	audioPlayer.setVolume(Float.valueOf(newVal+""));
        	updateSliderTrackStyle(slider);
        });
    }
    
    private void updateSliderTrackStyle(Slider slider) {
        double percentage = (slider.getValue() - slider.getMin()) / (slider.getMax() - slider.getMin());
        int percentInt = (int) (percentage * 100);

        Region track = (Region) slider.lookup(".track");
        if (track != null) {
            String style = String.format(
                "-fx-background-color: linear-gradient(to right, " +
                "#0071FF 0%%, " +
                "#00FF99 %d%%, " +
                "white %d%%, " +
                "white 100%%);",
                percentInt, percentInt
            );

            track.setStyle(style);
        }
    }
}
