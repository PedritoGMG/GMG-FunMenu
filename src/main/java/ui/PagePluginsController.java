package ui;

import core.data.AppData;
import core.triggers.AbstractTrigger;
import core.triggers.TriggerDTO;
import core.triggers.TriggerFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PagePluginsController implements Initializable {

    @FXML
    private VBox checkBoxContainerCommands, checkBoxContainerSounds;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        AppData appData = AppData.getInstance();
        List<TriggerDTO> triggers = appData.getTriggers().stream().toList();

        triggers.forEach(dto -> {
            AbstractTrigger trigger = TriggerFactory.getTrigger(dto.name());
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

            Label descriptionLabel = new Label(trigger.getDescription());
            descriptionLabel.getStyleClass().add("trigger-description");
            descriptionLabel.setWrapText(true);
            descriptionLabel.setMaxWidth(300);

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

            if (dto.audioPath() != null && !dto.audioPath().isEmpty()) {
                checkBoxContainerSounds.getChildren().add(triggerRow);
            } else {
                checkBoxContainerCommands.getChildren().add(triggerRow);
            }
        });
    }
}
