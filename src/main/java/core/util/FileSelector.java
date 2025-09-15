package core.util;

import core.audio.AudioPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class FileSelector {
    public static File selectFile(Stage stage, String title, String description, String... extensions) {
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

    public static File selectAudioFile(Stage stage) {
        return selectFile(stage,
                "Select an Audio File",
                "Audio Files " + AudioPlayer.GetSupportedAudioExtensionsList(),
                AudioPlayer.GetSupportedAudioFormatsArray());
    }
}
