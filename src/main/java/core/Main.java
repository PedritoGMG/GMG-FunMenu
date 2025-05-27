package core;

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
	public static AudioPlayerQueue playerTTS;
	public static AudioPlayerQueue playerMusic;
	public static AudioPlayer playerAudio;

    public static void main(String[] args) {
    	
    	try {
			playerTTS = new AudioPlayerQueue(audioDevice);
			playerMusic = new AudioPlayerQueue(audioDevice);
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