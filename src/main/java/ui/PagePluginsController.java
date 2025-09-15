package ui;

import core.audio.AudioPlayer;
import core.data.AppData;
import core.triggers.AbstractTrigger;
import core.triggers.AudioTrigger;
import core.triggers.TriggerDTO;
import core.triggers.TriggerFactory;
import core.util.FileSelector;
import core.util.HoverAnimator;
import core.util.Toast;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PagePluginsController implements Initializable {

    @FXML
    private VBox checkBoxContainerCommands, checkBoxContainerSounds;

    @FXML
    private Button addSoundKey;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        AppData appData = AppData.getInstance();
        List<TriggerDTO> triggers = appData.getTriggers()
                .stream()
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .toList();

        HoverAnimator.applySimpleHover(addSoundKey);

        triggers.forEach(dto -> {
            AbstractTrigger trigger = TriggerFactory.getTrigger(dto.name());
            boolean isAudioTrigger = dto.audioPath() != null && !dto.audioPath().isEmpty();
            int boxSize = isAudioTrigger ? 240: 300;

            if (trigger == null) return;

            HBox triggerRow = new HBox();
            triggerRow.getStyleClass().add("trigger-row");
            triggerRow.setAlignment(Pos.CENTER_LEFT);

            CheckBox enabledCheck = new CheckBox();
            enabledCheck.setSelected(dto.enabled());
            enabledCheck.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                appData.setTriggerEnabled(dto.name(), isNowSelected);
                trigger.setEnabled(isNowSelected);
            });

            VBox infoContainer = new VBox();
            infoContainer.getStyleClass().add("info-container");

            Label nameLabel = new Label(dto.name());
            nameLabel.getStyleClass().add("trigger-name");
            nameLabel.setTooltip(new Tooltip(trigger.getDescription()));
            nameLabel.setMaxWidth(boxSize);
            nameLabel.setMinWidth(boxSize);

            Label descriptionLabel = new Label(trigger.getDescription());
            descriptionLabel.getStyleClass().add("trigger-description");
            descriptionLabel.setWrapText(true);
            descriptionLabel.setMaxWidth(boxSize);
            descriptionLabel.setMinWidth(boxSize);

            CheckBox adminCheck = new CheckBox("Admin Only");
            adminCheck.setSelected(dto.adminOnly());
            adminCheck.getStyleClass().add("admin-check");
            adminCheck.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                appData.setTriggerOnlyAdmin(dto.name(), isNowSelected);
                trigger.setAdminOnly(isNowSelected);
            });

            infoContainer.getChildren().addAll(nameLabel, descriptionLabel, adminCheck);
            HBox.setHgrow(infoContainer, Priority.ALWAYS);

            triggerRow.getChildren().addAll(enabledCheck, infoContainer);

            if (isAudioTrigger) {
                SVGPath cross = new SVGPath();
                cross.setContent("M18 6L6 18 M6 6L18 18");
                cross.setStroke(javafx.scene.paint.Color.RED);
                cross.setStrokeWidth(2);
                cross.setFill(null);

                Button btnDelete = new Button();
                btnDelete.setGraphic(cross);
                btnDelete.getStyleClass().addAll("btn-cancel", "btn");
                btnDelete.setOnAction(ev -> {
                    TriggerFactory.unregister(dto.name());
                    refresh();
                });

                triggerRow.getChildren().add(btnDelete);

                checkBoxContainerSounds.getChildren().add(triggerRow);
            } else {
                checkBoxContainerCommands.getChildren().add(triggerRow);
            }
        });
    }

    private void refresh() {
        checkBoxContainerCommands.getChildren().clear();
        checkBoxContainerSounds.getChildren().clear();
        initialize(null, null);
    }

    @FXML
    public void onAddSoundKey() {
        TextField inputCommandName = new TextField();
        inputCommandName.setPromptText("Enter command name (max 50 chars)");
        inputCommandName.setPrefWidth(300);

        inputCommandName.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() > 50) {
                inputCommandName.setText(oldText);
            } else {
                inputCommandName.setText(newText.toUpperCase());
            }
        });

        TextField inputFilePath = new TextField();
        inputFilePath.setPromptText("Select file...");
        inputFilePath.setPrefWidth(250);
        inputFilePath.setEditable(false);

        inputFilePath.setOnMouseClicked(e -> {
            File selectedFile = FileSelector.selectAudioFile((Stage) inputFilePath.getScene().getWindow());
            if (selectedFile != null) {
                inputFilePath.setText(selectedFile.getAbsolutePath());
            }
        });

        try{
            CustomDialog.showDialog("Add Audio Command", "Fill the command name and select an audio file:",
                    stage -> {
                        String commandName = inputCommandName.getText().trim();
                        String filePath = inputFilePath.getText();

                        if (commandName.isEmpty()) {
                            Toast.showIn(stage, "Command name cannot be empty", 3000);
                            return;
                        }
                        if (filePath.isEmpty()) {
                            Toast.showIn(stage, "Please select a file", 3000);
                            return;
                        }
                        if (!TriggerFactory.isTriggerAvailable(commandName)) {
                            Toast.showIn(stage, "Command name cannot be duplicated", 3000);
                            return;
                        }

                        TriggerFactory.register(new AudioTrigger(commandName, true, false, filePath));
                        refresh();
                        stage.close();
                    },
                    vbox -> {
                        vbox.setSpacing(15);
                        vbox.setPadding(new Insets(10));
                        vbox.setAlignment(Pos.CENTER);

                        vbox.getChildren().addAll(inputCommandName, inputFilePath);
                    }
            );
        } catch (Exception ex) {
            //
        }
    }
}
