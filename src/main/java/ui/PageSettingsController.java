package ui;

import core.Main;
import core.data.AppData;
import core.game.Game;
import core.game.GameFactory;
import core.game.GameType;
import core.util.FileSelector;
import core.util.HoverAnimator;
import core.util.SteamUtils;
import core.util.Toast;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageSettingsController implements Initializable {

    @FXML
    private VBox adminContainer, banContainer;

    @FXML
    private Spinner<Integer> requestDuration, ttsLimit, musicLimit;

    @FXML
    private Button addAdmin, addBan, btnHelp;

    @FXML
    private ComboBox<Game> gameSelectorCB;

    @FXML
    private ComboBox<GameType> gameTypeCB;

    @FXML
    private TextField customInstallDir;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppData appData = AppData.getInstance();

        setupGameSelector(appData);

        List.of(addAdmin, addBan, btnHelp).forEach(HoverAnimator::applySimpleHover);

        appData.getAdminUsers().forEach(this::addAdminField);
        appData.getBannedUsers().forEach(this::addBanField);

        setupDurationSpinner();
        setupIntegerSpinner(ttsLimit, appData.getMaxQueueSizeTTS(),
                appData::setMaxQueueSizeTTS,
                Main.playerTTS::setMaxQueueSize);
        setupIntegerSpinner(musicLimit, appData.getMaxQueueSizeMUSIC(),
                appData::setMaxQueueSizeMUSIC,
                Main.playerMusic::setMaxQueueSize);
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

    @FXML
    private void onSelectCustomInstallDir() {
        if (!GameType.CUSTOM.equals(gameTypeCB.getValue())) return;

        Path selected = FileSelector.selectDirectory((Stage) customInstallDir.getScene().getWindow(),
                "Select Game Directory",
                AppData.getInstance().getInstallDir());

        if (selected != null) {
            AppData.getInstance().setInstallDir(selected);
            customInstallDir.setText(selected.toString());
        }
    }

    @FXML
    private void OnHelp() throws IOException {
        if (AppData.getInstance().getGameSelector() == null) return;
        CustomDialog.showDialog(
                "Game Setup Instructions",
                "Each game has its own configuration (many of them are similar)." +
                        " But just to be sure, click the '?' button if you want to verify that everything is set correctly.\n" +
                        "Below, you can find instructions on how to configure the launch parameters: ",
                null,
                rootVBox -> {
                    StackPane stack = new StackPane();
                    stack.setPadding(new Insets(10));

                    VBox vbox = new VBox(15);
                    vbox.setAlignment(Pos.TOP_CENTER);

                    Label instructions = new Label(
                            "To configure the game launch correctly, follow these steps:\n\n" +
                                    "1. Open Steam and go to your Library.\n" +
                                    "2. Right-click on the game and select 'Properties'.\n" +
                                    "3. In the 'General' tab, find 'Launch Options'.\n" +
                                    "4. Copy the text below and paste it into the Launch Options field.\n" +
                                    "5. Close the window and start the game."
                    );
                    instructions.setWrapText(true);
                    instructions.setMaxWidth(450);
                    instructions.setStyle("-fx-text-fill: white;");

                    String message = AppData.getInstance().getGameSelector().getSetupMessage();
                    TextFlow extraMessageFlow = new TextFlow();
                    extraMessageFlow.setMaxWidth(450);

                    Pattern urlPattern = Pattern.compile("(https?://[\\w\\-\\.\\?\\=\\&/%]+)");
                    Matcher matcher = urlPattern.matcher(message);

                    int lastEnd = 0;
                    while (matcher.find()) {
                        if (matcher.start() > lastEnd) {
                            Text t = new Text(message.substring(lastEnd, matcher.start()));
                            t.setFill(Color.WHITE);
                            t.setStyle("-fx-font-weight: bold;");
                            extraMessageFlow.getChildren().add(t);
                        }

                        String url = matcher.group(1);
                        Hyperlink link = new Hyperlink(url);
                        link.setOnAction(e -> {
                            if (Desktop.isDesktopSupported()) {
                                try {
                                    Desktop.getDesktop().browse(new URI(url));
                                } catch (IOException ex) {
                                    //
                                } catch (URISyntaxException ex) {
                                    //
                                }
                            }
                        });
                        extraMessageFlow.getChildren().add(link);

                        lastEnd = matcher.end();
                    }

                    if (lastEnd < message.length()) {
                        Text t = new Text(message.substring(lastEnd));
                        t.setFill(Color.WHITE);
                        t.setStyle("-fx-font-weight: bold;");
                        extraMessageFlow.getChildren().add(t);
                    }

                    TextField launchCommand = new TextField(AppData.getInstance().getGameSelector().getLaunchParameters());
                    launchCommand.setEditable(false);
                    launchCommand.setPrefWidth(400);

                    ImageView referenceImage = new ImageView(new Image(
                            getClass().getResourceAsStream("/images/launch_options_example.png")
                    ));
                    referenceImage.setFitWidth(500);
                    referenceImage.setPreserveRatio(true);
                    referenceImage.setSmooth(true);
                    referenceImage.setCache(true);

                    CheckBox dontShowAgain = new CheckBox("Do not show this dialog when clicking a game");
                    dontShowAgain.setWrapText(true);
                    dontShowAgain.setMaxWidth(450);
                    dontShowAgain.setSelected(AppData.getInstance().isSkipGameSetupDialog());
                    dontShowAgain.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                        AppData.getInstance().setSkipGameSetupDialog(isSelected);
                    });

                    vbox.getChildren().addAll(instructions, extraMessageFlow, launchCommand, dontShowAgain);

                    SteamUtils.getSmallestGameIcon(AppData.getInstance().getGameSelector().getAppId()).ifPresent(path -> {
                        ImageView gameIcon = new ImageView(new Image(path.toUri().toString(), 32, 32, true, true));
                        gameIcon.setSmooth(true);
                        gameIcon.setCache(true);

                        StackPane.setAlignment(gameIcon, Pos.TOP_RIGHT);
                        StackPane.setMargin(gameIcon, new Insets(5));
                        stack.getChildren().add(gameIcon);
                    });

                    stack.getChildren().add(vbox);

                    HBox hbox = new HBox(15);
                    hbox.setAlignment(Pos.TOP_CENTER);
                    hbox.getChildren().addAll(referenceImage, stack);

                    rootVBox.getChildren().add(hbox);
                }
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
        userField.setWrappingWidth(165);
        userField.setLayoutY(300);
        userField.setSmooth(true);
        userField.getStyleClass().add("name-text");
        userField.setOnMouseEntered(e -> {
            userField.getStyleClass().add("name-text-hover");
        });
        userField.setOnMouseExited(e -> {
            userField.getStyleClass().remove("name-text-hover");
        });
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

    private void setupIntegerSpinner(Spinner<Integer> spinner, int initialValue, IntConsumer setter, IntConsumer applyToPlayer) {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, initialValue, 1);

        spinner.setValueFactory(factory);
        spinner.setEditable(true);

        TextField editor = spinner.getEditor();
        editor.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*")) editor.setText(oldText);
        });

        factory.valueProperty().addListener((obs, oldValue, newValue) -> {
            setter.accept(newValue);
            applyToPlayer.accept(newValue);
        });
    }

    private void setupGameSelector(AppData appData) {

        customInstallDir.disableProperty().bind(
                Main.isReading.or(
                        Bindings.createBooleanBinding(
                                () -> !GameType.CUSTOM.equals(gameTypeCB.getValue()),
                                gameTypeCB.valueProperty()
                        )
                )
        );
        gameTypeCB.disableProperty().bind(Main.isReading);
        gameSelectorCB.disableProperty().bind(Main.isReading);

        gameTypeCB.getItems().addAll(GameType.values());
        gameTypeCB.setValue(appData.getGameType() != null ? appData.getGameType() : GameType.OFFICIAL);

        gameTypeCB.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCustom = GameType.CUSTOM.equals(newVal);
            AppData appDt = AppData.getInstance();
            appDt.setGameType(newVal);

            if (isCustom) {
                customInstallDir.setText(appData.getInstallDir() != null ? appData.getInstallDir().toString() : "");
            } else {
                Game selectedGame = gameSelectorCB.getSelectionModel().getSelectedItem();
                if (selectedGame != null) {
                    appDt.setGameSelector(selectedGame);
                    appDt.setInstallDir(selectedGame.getInstallDir());
                    appDt.setLogFile(selectedGame.getLogFile());
                    customInstallDir.setText(selectedGame.getInstallDir() != null ? selectedGame.getInstallDir().toString() : "");
                }
            }
        });
        gameTypeCB.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(GameType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });
        gameTypeCB.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(GameType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });

        if (appData.getInstallDir() != null && GameType.CUSTOM.equals(appData.getGameType())) {
            customInstallDir.setText(appData.getInstallDir().toString());
        }

        customInstallDir.textProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> customInstallDir.positionCaret(customInstallDir.getText().length()));
            if (GameType.CUSTOM.equals(gameTypeCB.getValue()) && !newVal.isBlank()) {
                Path path = Path.of(newVal);
                AppData.getInstance().setInstallDir(path);
            }
        });

        gameSelectorCB.getItems().addAll(GameFactory.getAllGamesSorted());

        gameSelectorCB.valueProperty().addListener((obs, oldGame, newGame) -> {
            if (newGame != null) {
                AppData appDt = AppData.getInstance();
                appDt.setGameSelector(newGame);
                if (GameType.OFFICIAL.equals(gameTypeCB.getValue())) {
                    appDt.setInstallDir(newGame.getInstallDir());
                    appDt.setLogFile(newGame.getLogFile());
                    customInstallDir.setText(newGame.getInstallDir() != null ? newGame.getInstallDir().toString() : "");
                }
                if (!AppData.getInstance().isSkipGameSetupDialog()) {
                    Platform.runLater(() -> {
                        try {
                            OnHelp();
                        } catch (IOException e) {
                            //
                        }
                    });
                }
            }
        });

        gameSelectorCB.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Game item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setDisable(false);
                } else {
                    setText(item.getName());
                    if (item.getInstallDir() != null) {
                        SteamUtils.getSmallestGameIcon(item.getAppId()).ifPresent(path -> {
                            Image img = new Image(path.toUri().toString(), 32, 32, true, true);
                            setGraphic(new ImageView(img));
                        });
                    } else {
                        setGraphic(null);
                    }
                    setDisable(item.getInstallDir() == null);
                }
            }
        });

        gameSelectorCB.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Game item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getName());
                    if (item.getInstallDir() != null) {
                        SteamUtils.getSmallestGameIcon(item.getAppId()).ifPresent(path -> {
                            Image img = new Image(path.toUri().toString(), 32, 32, true, true);
                            setGraphic(new ImageView(img));
                        });
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        Game baseGame = appData.getGameSelector();
        if (baseGame != null) {
            gameSelectorCB.getSelectionModel().select(baseGame);
        }
    }
}
