package ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import core.Main;
import core.audio.AudioPlayer;
import core.audio.plugin.TTS;
import core.audio.plugin.YoutubeAudioDownloader;
import core.data.AppData;
import core.file.FileWatcher;
import core.file.KeywordTriggerListener;
import core.game.Game;
import core.game.GameType;
import core.game.capable.PatchCapable;
import core.util.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class PageMainController implements Initializable {
	
    @FXML
    private TextField fileText;

    @FXML
    private Button
    	playTTS, resumeTTS, stopTTS, addTTS,
    	playMusic, resumeMusic, stopMusic, addMusic,
    	playAudio, resumeAudio, stopAudio, addAudio;
    
    @FXML
    private ToggleButton toggleButton;
    
    @FXML
    private Slider sliderTTS, sliderMusic, sliderAudio;

	@FXML
	private Canvas audioCanvas;

	private static MicrophoneCapture micCapture = null;
	
    @Override
    public void initialize(URL location, ResourceBundle resources) {
		if (micCapture == null) {
			micCapture = new MicrophoneCapture(audioCanvas);
			micCapture.start();
		} else {
			micCapture.setCanvas(audioCanvas);
		}
		micCapture.resetBars();

		AppData appdata = AppData.getInstance();

		if (appdata.getLogFile() != null) fileText.setText(appdata.getLogFile().toString());

		fileText.disableProperty().bind(
				Main.isReading.or(
						Bindings.createBooleanBinding(
								() -> !GameType.CUSTOM.equals(appdata.getGameType()),
								Main.isReading
						)
				)
		);

		boolean isCustom = GameType.CUSTOM.equals(appdata.getGameType());
		fileText.textProperty().addListener((obs, oldVal, newVal) -> {
			if (isCustom && newVal != null && !newVal.isBlank()) {
				appdata.setLogFile(new File(newVal).toPath());
			}
		});

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

		buttons.forEach(list -> list.forEach(HoverAnimator::apply));
        
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
					                        Toast.showIn(stage, "Download failed: "+ex.getMessage(), 8000);
					                        //System.err.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
					                        //System.err.println("at " + ex.getStackTrace()[0]);
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
						File file = FileSelector.selectAudioFile((Stage) addMusic.getScene().getWindow());

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
        	File file = FileSelector.selectAudioFile((Stage) addAudio.getScene().getWindow());

        	if (file!=null) {
        		try {
    				Main.playerAudio.play(file);
    			} catch (Exception e) {
    				System.err.println(e.getMessage());
    			}
			}
        });

		toggleButton.setText(Main.isReading() ? "Stop Reading" : "Start Reading");
		toggleButton.setSelected(Main.isReading());
		toggleButton.getStyleClass().add(Main.isReading() ? "button-dark2" : "button-dark");
		toggleButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
			Main.setReading(newVal);
			System.out.println("Reading: " + newVal + " | File: " + appdata.getLogFile() + " | " + LocalDateTime.now());
			if (Main.isReading()) {
				toggleButton.setText("Stop Reading");
				toggleButton.getStyleClass().remove("button-dark");
				toggleButton.getStyleClass().add("button-dark2");

				if (appdata.getLogFile() != null) {
					Game game = appdata.getGameSelector();
					if (game instanceof PatchCapable) {
						PatchCapable patchable = ((PatchCapable) game);
						if (appdata.getGameType().isOfficial()) {
							patchable.patchGameFiles();
							appdata.setConsoleSender(new ConsoleSenderUtil(patchable.getCfgFolder(), "funMenu.cfg"));
						} else if (appdata.getGameType().isCustom()) {
							Path cfgFolder = PatchCapable.detectCfgFolder(appdata.getInstallDir());
							if (cfgFolder != null) {
								PatcherUtil.apply(cfgFolder, patchable.getSetupCommands());
								appdata.setConsoleSender(new ConsoleSenderUtil(cfgFolder, "funMenu.cfg"));
							}
						}
					}

					appdata.getConsoleSender().start();
					Main.fileWatcher = new FileWatcher(appdata.getLogFile().toFile(), line -> KeywordTriggerListener.getInstance().onNewLine(line));
				}

			} else {
				toggleButton.setText("Start Reading");
				toggleButton.getStyleClass().remove("button-dark2");
				toggleButton.getStyleClass().add("button-dark");
				if (Main.fileWatcher != null) {
					Main.fileWatcher.stop();
					Main.fileWatcher = null;
				}
				if (appdata.getConsoleSender() != null) {
					appdata.getConsoleSender().stop();
					appdata.setConsoleSender(new ConsoleSenderUtil());
				}
			}
		});
		toggleButton.disableProperty().bind(
				Bindings.createBooleanBinding(
						() -> appdata.getLogFile() == null
								|| appdata.getGameSelector() == null
				)
		);
        
    }

    
    @FXML
    private void clickSelectFile() {
		AppData appData = AppData.getInstance();
		File file = FileSelector.selectFile((Stage) fileText.getScene().getWindow(), "Select any file to read", null);
		if (file == null) return;
		appData.setLogFile(file.toPath());
    	if (appData.getLogFile()!=null) {
    		fileText.setText(file.getAbsolutePath().toString());
		}
    }

	private void setupSlider(Slider slider, AudioPlayer audioPlayer, float initialVolume, Consumer<Float> setAppDataVolume) {
		audioPlayer.setVolume(initialVolume);

		slider.setMin(0);
		slider.setMax(1);
		slider.setValue(initialVolume);

		Platform.runLater(() -> {
			updateSliderTrackStyle(slider);

			Node thumb = slider.lookup(".thumb");
			if (thumb != null) {
				HoverAnimator.applyThumbHover(thumb);
			}
		});

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
