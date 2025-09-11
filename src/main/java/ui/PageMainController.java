package ui;

import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.sound.sampled.LineUnavailableException;

import core.Main;
import core.audio.AudioPlayer;
import core.audio.plugin.TTS;
import core.audio.plugin.YoutubeAudioDownloader;
import core.data.AppData;
import core.file.FileWatcher;
import core.file.KeywordTriggerListener;
import core.util.Toast;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class PageMainController implements Initializable{
	
    @FXML
    private TextField fileText;

    @FXML
    private Button btnEnviarTTS, btnSeleccionarArchivo, btnYT,
    	playTTS, resumeTTS, stopTTS, addTTS,
    	playMusic, resumeMusic, stopMusic, addMusic,
    	playAudio, resumeAudio, stopAudio, addAudio;
    
    @FXML
    private ToggleButton toggleButton;
    
    @FXML
    private Slider sliderTTS, sliderMusic, sliderAudio;
	
    @Override
    public void initialize(URL location, ResourceBundle resources) {
		AppData appdata = AppData.getInstance();

		if (Main.file != null)
			fileText.setText(Main.file.getAbsolutePath());

		setupSlider(sliderTTS, Main.playerTTS.getAudioPlayer(), appdata.getTtsVolume(), appdata::setTtsVolume);
        setupSlider(sliderMusic, Main.playerMusic.getAudioPlayer(), appdata.getMusicVolume(), appdata::setMusicVolume);
        setupSlider(sliderAudio, Main.playerAudio, appdata.getAudioVolume(), appdata::setAudioVolume);
        
        List<List<Button>> buttons = List.of(
        		List.of(playTTS, 	resumeTTS, 		stopTTS, 	addTTS), 
        		List.of(playMusic, 	resumeMusic, 	stopMusic, 	addMusic), 
        		List.of(playAudio, 	resumeAudio, 	stopAudio, 	addAudio)
        		);
        
        List<AudioPlayer> audioPlayers = List.of(
        		Main.playerTTS.getAudioPlayer(),
        		Main.playerMusic.getAudioPlayer(),
        		Main.playerAudio
        		);
        
        for (int i = 0; i < audioPlayers.size(); i++) {
        	AudioPlayer audioPlayer = audioPlayers.get(i);
        	buttons.get(i).get(0).setOnAction(event -> audioPlayer.resume());
        	buttons.get(i).get(1).setOnAction(event -> audioPlayer.pause());
        	buttons.get(i).get(2).setOnAction(event -> audioPlayer.stop());
		}
        
        addTTS.setOnAction(event -> {
			try {
				TextField inputTTS = new TextField();
				inputTTS.setPromptText("Enter the text here...");
				TextField numberFieldEngine = new TextField("4");
				TextField numberFieldLang = new TextField("1");
				TextField numberFieldVoice = new TextField("5");
				List<TextField> numberFields = List.of(numberFieldEngine, numberFieldLang, numberFieldVoice);
				numberFields.forEach(t -> {
					t.textProperty().addListener((obs, oldValue, newValue) -> {
					    if (!newValue.matches("\\d*")) {
					        t.setText(newValue.replaceAll("[^\\d]", ""));
					    }
					});

					t.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
					    if (!isFocused) {
					        if (t.getText().isEmpty()) {
					            t.setText("0");
					        }
					    }
					});
				});
				
				CustomDialog.showDialog("Add TTS to the Queue", "Indicates the type of voice and text to be played: ",
						stage -> {
							stage.getScene().getRoot().setDisable(true);
							String text = inputTTS.getText();
							TTS.request(
								    text,
								    Integer.parseInt(numberFieldEngine.getText()),
								    Integer.parseInt(numberFieldLang.getText()),
								    Integer.parseInt(numberFieldVoice.getText()),
								    file -> {
								        Main.playerTTS.enqueue(file);
								        stage.close();
								    },
								    ex -> {
								        Toast.showIn(stage, "Voice generation fails", 8000);
								        stage.getScene().getRoot().setDisable(false);
								    }
								);
						
						}, vbox -> {
							HBox hBoxNumbers = new HBox(10, numberFieldEngine, numberFieldLang, numberFieldVoice);
							HBox hboxAll = new HBox(20, inputTTS, hBoxNumbers);
							hBoxNumbers.getChildren().addAll();
							hBoxNumbers.setAlignment(Pos.CENTER);
							hBoxNumbers.setPrefWidth(200);
							hboxAll.getChildren().addAll();
							hboxAll.setAlignment(Pos.CENTER);

							inputTTS.setPrefWidth(450);

							vbox.setSpacing(15);
							vbox.setPadding(new Insets(10));
							vbox.setAlignment(Pos.CENTER);
							vbox.getChildren().addAll(hboxAll);
						});
				
			} catch (IOException e) {
				//e.printStackTrace();
			}
		});
        
		addMusic.setOnAction(event -> {
			try {
				
				CustomDialog.showDialog("Add Music to the Queue", "Select what type of addition you want to make: ", null, vbox -> {

					Button btn1 = new Button("Youtube URL");
					Button btn2 = new Button("Audio File");
					
					TextField inputYoutubeURL = new TextField();
					inputYoutubeURL.setPromptText("https://www.youtube.com/watch?v=");
					btn1.setOnAction(eventBtn -> {
					    try {
					        CustomDialog.showDialog("Add Youtube Music to the Queue",
					            "Put the link of the music you want to add: ", stage -> {

					                String url = inputYoutubeURL.getText();

					                stage.getScene().getRoot().setDisable(true);
					                YoutubeAudioDownloader.request(
					                    url,
					                    () -> {
					                    	Toast.showIn(stage, "Invalid Url", 8000);
					                    	stage.getScene().getRoot().setDisable(false);
					                    	},
					                    () -> {
					                        Toast.showIn(stage, "Downloading...", 8000);
					                    },
					                    file -> {
					                        Main.playerMusic.enqueue(file);
					                        stage.close();
					                        Stage dialogStage = (Stage) btn2.getScene().getWindow();
					                        dialogStage.close();
					                    },
					                    ex -> {
					                        Toast.showIn(stage, "Download failed", 8000);
					                        System.err.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
					                        System.err.println("at " + ex.getStackTrace()[0]);
					                        stage.getScene().getRoot().setDisable(false);
					                    }
					                );

					            }, vboxYoutube -> {
					                vboxYoutube.getChildren().addAll(inputYoutubeURL);
					            });
					    } catch (IOException e) {
					        e.printStackTrace();
					    }
					});

					btn2.setOnAction(eventBtn -> {
						File file = selectAudioFile((Stage) addMusic.getScene().getWindow());

						if (file != null) {
							try {
								Main.playerMusic.enqueue(file);
								Stage dialogStage = (Stage) btn2.getScene().getWindow();
								dialogStage.close();
							} catch (Exception e) {
								System.err.println(e.getMessage());
							}
						}
					});

					btn1.getStyleClass().add("button-dark");
					btn2.getStyleClass().add("button-dark");

					HBox hboxBtns = new HBox(10, btn1, btn2);
					hboxBtns.setAlignment(Pos.CENTER);

					vbox.setSpacing(15);
					vbox.setPadding(new Insets(10));
					vbox.setAlignment(Pos.CENTER);
					vbox.getChildren().addAll(hboxBtns);

				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
        
        addAudio.setOnAction(event -> {
        	File file = selectAudioFile((Stage) addAudio.getScene().getWindow());

        	if (file!=null) {
        		try {
    				Main.playerAudio.play(file);
    			} catch (Exception e) {
    				System.err.println(e.getMessage());
    			}
			}
        });

		File currentFile = Main.file;

		toggleButton.setText(Main.isReading ? "Stop Reading" : "Start Reading");
		toggleButton.setSelected(Main.isReading);
		toggleButton.setDisable(Main.file == null);
		toggleButton.getStyleClass().add(Main.isReading ? "button-dark2" : "button-dark");
		toggleButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
			System.out.println("Toggle Reading: " + newVal);
			Main.isReading = newVal;
			if (newVal) {
				toggleButton.setText("Stop Reading");
				toggleButton.getStyleClass().remove("button-dark");
				toggleButton.getStyleClass().add("button-dark2");
				if (currentFile != null) {
					KeywordTriggerListener listener = KeywordTriggerListener.getInstance();
					Main.fileWatcher = new FileWatcher(currentFile, line -> listener.onNewLine(line));
				}
			} else {
				toggleButton.setText("Start Reading");
				toggleButton.getStyleClass().remove("button-dark2");
				toggleButton.getStyleClass().add("button-dark");
				if (Main.fileWatcher != null) {
					Main.fileWatcher.stop();
					Main.fileWatcher = null;
				}
			}
		});
        
    }
    
    private File selectAudioFile(Stage stage) {
    	return selectFile(stage, "Select an Audio File", "Audio Files "+AudioPlayer.GetSupportedAudioExtensionsList(), AudioPlayer.GetSupportedAudioFormatsArray());
    }
    
    private File selectFile(Stage stage, String title, String description, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        if (extensions != null && extensions.length > 0) {
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(description, extensions);
            fileChooser.getExtensionFilters().add(extFilter);
        } else {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );
        }

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null && selectedFile.canRead()) {
            return selectedFile;
        }

        return null;
    }

    
    @FXML
    private void clickSelectFile() {
    	Main.file = selectFile((Stage) fileText.getScene().getWindow(), "Select any file to read", null);
    	if (Main.file!=null) {
    		fileText.setText(Main.file.getAbsolutePath());
    		toggleButton.setDisable(false);
		}
    	
    }

	private void setupSlider(Slider slider, AudioPlayer audioPlayer, float initialVolume, Consumer<Float> setAppDataVolume) {
		audioPlayer.setVolume(initialVolume);

		slider.setMin(0);
		slider.setMax(1);
		slider.setValue(initialVolume);

		Platform.runLater(() -> updateSliderTrackStyle(slider));

		slider.valueProperty().addListener((obs, oldVal, newVal) -> {
			float vol = newVal.floatValue();
			audioPlayer.setVolume(vol);
			setAppDataVolume.accept(vol);
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
