package ui;

import core.data.AppData;
import core.file.AudioTriggerData;
import core.file.KeywordTrigger;
import core.file.KeywordTriggerListener;
import core.file.TriggerData;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class PagePluginsController implements Initializable {

    @FXML
    private VBox checkBoxContainerCommands, checkBoxContainerSounds;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ArrayList<TriggerData> triggers = AppData.getInstance().getTriggers();

        triggers.forEach(trigger -> {
            CheckBox check = new CheckBox(trigger.getName());
            check.setSelected(trigger.isEnabled());

            check.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                trigger.setEnabled(isNowSelected);
            });

            if (trigger instanceof AudioTriggerData) {
                checkBoxContainerSounds.getChildren().add(check);
            } else {
                checkBoxContainerCommands.getChildren().add(check);
            }
        });
    }
}
