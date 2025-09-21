package core;

import java.io.File;
import java.io.PrintStream;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import core.audio.AudioPlayer;
import core.audio.AudioPlayerQueue;
import core.data.AppData;
import core.data.DataManager;
import core.data.GlobalConsoleOutputStream;
import core.file.FileWatcher;
import core.file.KeywordTriggerListener;
import core.game.GameFactory;
import core.keybindings.GlobalKeyListener;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

	public static String micDevice = "CABLE Output";
	public static String audioDevice = "CABLE Input";
	public static FileWatcher fileWatcher = null;
	public static final BooleanProperty isReading = new SimpleBooleanProperty(false);
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

		PrintStream out = new PrintStream(new GlobalConsoleOutputStream(Color.WHITE), true);
		PrintStream err = new PrintStream(new GlobalConsoleOutputStream(Color.RED), true);
		//System.setOut(out);
		//System.setErr(err);

		GameFactory.getAllGames();

		AppData appData = AppData.getInstance();
		appData.load();

		KeywordTriggerListener.getInstance();
		GlobalKeyListener.register();

    	try {
			playerTTS = new AudioPlayerQueue(audioDevice);
			playerTTS.setVolume(appData.getTtsVolume());
			playerTTS.setMaxQueueSize(appData.getMaxQueueSizeTTS());
			playerTTS.getAudioPlayer().setAudioListener(() -> {
				playerTTS.onAudioFinished();
			});
			playerMusic = new AudioPlayerQueue(audioDevice);
			playerMusic.setVolume(appData.getMusicVolume());
			playerMusic.setMaxQueueSize(appData.getMaxQueueSizeMUSIC());
			playerMusic.getAudioPlayer().setAudioListener(() -> {
				playerMusic.onAudioFinished();
			});
			playerAudio = new AudioPlayer(audioDevice);
			playerAudio.setVolume(appData.getAudioVolume());
			
			//ShutDown Hook
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				OnExit();
	        }));
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
		stage.setOnCloseRequest(event -> OnExit());
        stage.show();
    }
    
    public static void deleteTempFiles() {
        File folder = Main.TEMP_DIR;
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File archivo : files) {
                    if (archivo.isFile()) {
                        archivo.delete();
                    }
                }
            }
        }
    }
    
    public static void OnExit() {
    	playerTTS.stopAndClear();
		playerMusic.stopAndClear();
		playerAudio.stop();

		DataManager.save(AppData.getInstance());
		
		deleteTempFiles();

		try {
			if (GlobalScreen.isNativeHookRegistered()) {
				GlobalScreen.unregisterNativeHook();
				System.out.println("Native hook unregistered successfully");
			}
		} catch (NativeHookException ex) {
			System.err.println("Error unregistering native hook: " + ex.getMessage());
		}
	}

	public static boolean isReading() {
		return isReading.get();
	}

	public static void setReading(boolean value) {
		isReading.set(value);
	}
}