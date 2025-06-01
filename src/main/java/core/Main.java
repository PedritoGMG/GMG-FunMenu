package core;

import java.io.File;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
	
	public static String audioDevice = "CABLE Input";
	public static int maxDuration = 600;
	public static String delimiter = " :  ";
	public static FileWatcher fileWatcher = null;
	public static File file = null;
	public static final File TEMP_DIR = new File("temp-PGMG_FM-Downloads");
	static {
	    if (!TEMP_DIR.exists()) {
	        TEMP_DIR.mkdirs();
	    }
	}

	
	public static AudioPlayerQueue playerTTS;
	public static AudioPlayerQueue playerMusic;
	public static AudioPlayer playerAudio;
	
    public static void main(String[] args) {
    	
    	try {
			playerTTS = new AudioPlayerQueue(audioDevice);
			playerTTS.getAudioPlayer().setAudioListener(() -> {
				playerTTS.onAudioFinished();
			});
			playerMusic = new AudioPlayerQueue(audioDevice);
			playerMusic.getAudioPlayer().setAudioListener(() -> {
				playerMusic.onAudioFinished();
			});
			playerAudio = new AudioPlayer(audioDevice);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
    	
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ui/main.fxml"));
        Scene scene = new Scene(root);
        
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
		
        stage.setTitle("PGMG-FunPad");
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        
        stage.setScene(scene);
        stage.show();
    }
}