package ui;

import core.data.AppData;
import core.util.Toast;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;

public class PageSettingsController implements Initializable {

    @FXML
    private VBox adminContainer, banContainer;

    @FXML
    private Spinner<Integer> requestDuration, ttsLimit, musicLimit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppData appData = AppData.getInstance();
        appData.getAdminUsers().forEach(this::addAdminField);
        appData.getBannedUsers().forEach(this::addBanField);

        setupDurationSpinner();
        setupIntegerSpinner(ttsLimit, appData.getMaxQueueSizeTTS(), appData::setMaxQueueSizeTTS);
        setupIntegerSpinner(musicLimit, appData.getMaxQueueSizeMUSIC(), appData::setMaxQueueSizeMUSIC);
    }

    private void showAddUserDialog(String title, String message, Function<String, Boolean> addAction, Consumer<String> addFieldAction) throws IOException {
        TextField inputField = new TextField();
        inputField.setPromptText("Enter name...");

        CustomDialog.showDialog(title, message,
                stage -> {
                    String name = inputField.getText().trim();
                    if (name.isEmpty()) {
                        Toast.showIn(stage, "Name cannot be empty", 3000);
                        return;
                    }

                    if (addAction != null) {
                        boolean added = addAction.apply(name);
                        if (added) {
                            addFieldAction.accept(name);
                            stage.close();
                        } else {
                            Toast.showIn(stage, "Name cannot be duplicated", 3000);
                        }
                    }
                },
                vbox -> {
                    vbox.setSpacing(15);
                    vbox.setPadding(new Insets(10));
                    vbox.setAlignment(Pos.CENTER);
                    inputField.setPrefWidth(300);
                    vbox.getChildren().add(inputField);
                }
        );
    }

    @FXML
    public void OnAddAdmin() throws IOException {
        showAddUserDialog(
                "Add Administrator",
                "Enter the name of the new administrator:",
                AppData.getInstance()::addAdmin,
                this::addAdminField
        );
    }

    @FXML
    public void OnAddBan() throws IOException {
        showAddUserDialog(
                "Add Banned User",
                "Enter the name to ban:",
                AppData.getInstance()::addBan,
                this::addBanField
        );
    }

    private void addAdminField(String name) {
        addUserField(name, adminContainer, AppData.getInstance()::removeAdmin);
    }

    private void addBanField(String name) {
        addUserField(name, banContainer, AppData.getInstance()::removeBan);
    }

    private void addUserField(String name, VBox container, Consumer<String> removeAction) {
        Text userField = new Text(name);
        userField.setOnMouseClicked(event -> {
            if (removeAction != null) {
                removeAction.accept(name);
                container.getChildren().remove(userField);
            }
        });
        container.getChildren().add(userField);
    }
    private void setupDurationSpinner() {
        AppData appData = AppData.getInstance();
        int initialSeconds = appData.getMaxDurationRequest();

        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, initialSeconds, 1);

        valueFactory.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer value) {
                if (value == null) return "00:00:00";
                int hours = value / 3600;
                int minutes = (value % 3600) / 60;
                int seconds = value % 60;
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }

            @Override
            public Integer fromString(String string) {
                try {
                    String[] parts = string.split(":");
                    int totalSeconds = 0;
                    if (parts.length == 3) {
                        totalSeconds += Integer.parseInt(parts[0]) * 3600;
                        totalSeconds += Integer.parseInt(parts[1]) * 60;
                        totalSeconds += Integer.parseInt(parts[2]);
                    } else if (parts.length == 2) {
                        totalSeconds += Integer.parseInt(parts[0]) * 60;
                        totalSeconds += Integer.parseInt(parts[1]);
                    } else if (parts.length == 1) {
                        totalSeconds += Integer.parseInt(parts[0]);
                    }
                    return totalSeconds;
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        });

        requestDuration.setValueFactory(valueFactory);

        valueFactory.valueProperty().addListener((obs, oldValue, newValue) -> appData.setMaxDurationRequest(newValue));
        TextField editor = requestDuration.getEditor();
        editor.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Integer value = valueFactory.getConverter().fromString(editor.getText());
                valueFactory.setValue(value);
            }
        });
    }

    private void setupIntegerSpinner(Spinner<Integer> spinner, int initialValue, java.util.function.IntConsumer setter) {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, initialValue, 1);

        spinner.setValueFactory(factory);
        spinner.setEditable(true);

        TextField editor = spinner.getEditor();
        editor.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*")) editor.setText(oldText);
        });

        factory.valueProperty().addListener((obs, oldValue, newValue) -> setter.accept(newValue));
    }
}
