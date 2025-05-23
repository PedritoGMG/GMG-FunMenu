package ui;

import core.audio.AudioPlayerType;
import core.data.AppData;
import core.keybindings.KeyBinding;
import core.triggers.AbstractTrigger;
import core.triggers.TriggerFactory;
import core.triggers.labels.MessageIsAudioPlayerType;
import core.triggers.labels.MessageIsFile;
import core.triggers.labels.NoMessageRequired;
import core.triggers.labels.RequiresMessage;
import core.util.FileSelector;
import core.util.HoverAnimator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class PageBindsController implements Initializable {

    @FXML
    private TableView bindsTable;

    @FXML
    private TableColumn<KeyBinding, Boolean> enabledColumn;

    @FXML
    private TableColumn<KeyBinding, String> keyColumn, actionTypeColumn, contextColumn;

    @FXML
    private Button addBind, removeBind;

    private ObservableList<KeyBinding> bindsObservable;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        bindsObservable = FXCollections.observableArrayList(AppData.getInstance().getBinds());
        bindsTable.setItems(bindsObservable);
        bindsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        removeBind.disableProperty().bind(
                Bindings.isEmpty(bindsTable.getSelectionModel().getSelectedItems())
        );

        enabledColumn.setCellValueFactory(cellData -> {
            KeyBinding bind = cellData.getValue();
            return new SimpleBooleanProperty(bind.isEnabled()) {
                @Override
                public void set(boolean value) {
                    super.set(value);
                    bind.setEnabled(value);
                    AppData.getInstance().setBinds(new ArrayList<>(bindsObservable));
                }
            };
        });
        enabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(enabledColumn));

        keyColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getKeyDisplay())
        );
        keyColumn.setCellFactory(col -> new TableCell<>() {
            private final Button button = new Button();
            {
                HoverAnimator.apply(button);
                button.getStyleClass().addAll("control-btn", "btn");
                button.setPrefWidth(130);
                button.setPrefHeight(36);
                button.setMinWidth(130);
                button.setMinHeight(36);
            }

            @Override
            protected void updateItem(String key, boolean empty) {
                super.updateItem(key, empty);
                if (empty || key == null) {
                    setGraphic(null);
                } else {
                    button.setText(key);
                    button.setOnAction(e -> {
                        try {
                            onAssignKey(getTableView().getItems().get(getIndex()));
                        } catch (IOException ex) {
                            //
                        }
                    });
                    setGraphic(button);
                }
            }
        });

        actionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        actionTypeColumn.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> comboBox = new ComboBox<>();

            @Override
            protected void updateItem(String actionType, boolean empty) {
                super.updateItem(actionType, empty);
                if (empty || actionType == null) {
                    setGraphic(null);
                } else {
                    List<String> triggerNames = TriggerFactory.getOrderedTriggers()
                            .stream()
                            .map(AbstractTrigger::getName)
                            .toList();
                    comboBox.setItems(FXCollections.observableArrayList(triggerNames));
                    comboBox.setValue(actionType);

                    comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                        KeyBinding bind = getTableView().getItems().get(getIndex());
                        if (bind != null) {
                            bind.setActionType(newVal);
                            AppData.getInstance().setBinds(new ArrayList<>(bindsObservable));

                            getTableView().refresh();
                        }
                    });

                    setGraphic(comboBox);
                }
            }
        });


        contextColumn.setCellValueFactory(new PropertyValueFactory<>("context"));
        contextColumn.setCellFactory(col -> new TableCell<KeyBinding, String>() {

            private final TextField textField = new TextField();
            private final ComboBox<AudioPlayerType> comboBox = new ComboBox<>();

            {
                comboBox.prefWidthProperty().bind(contextColumn.widthProperty());
                textField.setPromptText("Add some context...");
                textField.focusedProperty().addListener((obs, oldFocus, newFocus) -> {
                    if (!newFocus) saveValue();
                });
                textField.setOnAction(e -> saveValue());
            }

            private void saveValue() {
                KeyBinding bind = getTableView().getItems().get(getIndex());
                if (bind != null) {
                    bind.setContext(textField.getText());
                    AppData.getInstance().setBinds(new ArrayList<>(getTableView().getItems()));
                }
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null) {
                    setGraphic(null);
                    return;
                }

                KeyBinding bind = getTableView().getItems().get(getIndex());
                if (bind == null) return;

                AbstractTrigger trigger = TriggerFactory.getTrigger(bind.getActionType());
                if (trigger == null) {
                    textField.setDisable(true);
                    setGraphic(textField);
                    return;
                }

                // PRIORIDAD: ComboBox > File > TextField > Disabled
                if (trigger instanceof MessageIsAudioPlayerType) {
                    comboBox.getItems().setAll(AudioPlayerType.values());
                    AudioPlayerType selectedType = null;
                    if (item != null && !item.isEmpty()) {
                        try {
                            selectedType = AudioPlayerType.valueOf(item.trim().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            selectedType = null;
                        }
                    }
                    comboBox.setValue(selectedType);
                    comboBox.setOnAction(e -> {
                        KeyBinding k = getTableView().getItems().get(getIndex());
                        if (k != null && comboBox.getValue() != null) {
                            k.setContext(comboBox.getValue().name());
                            AppData.getInstance().setBinds(new ArrayList<>(getTableView().getItems()));
                        }
                    });
                    setGraphic(comboBox);

                } else if (trigger instanceof MessageIsFile) {
                    textField.setDisable(false);
                    textField.setText(item);
                    textField.setOnMouseClicked(e -> {
                        File selectedFile = FileSelector.selectAudioFile((Stage) textField.getScene().getWindow());
                        if (selectedFile != null) {
                            textField.setText(selectedFile.getAbsolutePath());
                            bind.setContext(selectedFile.getAbsolutePath());
                            AppData.getInstance().setBinds(new ArrayList<>(getTableView().getItems()));
                        }
                    });
                    setGraphic(textField);

                } else if (trigger instanceof RequiresMessage) {
                    textField.setText(item);
                    textField.setDisable(false);
                    textField.setOnMouseClicked(null);
                    setGraphic(textField);

                } else if (trigger instanceof NoMessageRequired) {
                    textField.clear();
                    saveValue();
                    textField.setDisable(true);
                    setGraphic(textField);

                } else {
                    textField.setText(item);
                    textField.setDisable(false);
                    setGraphic(textField);
                }
            }
        });
    }

    @FXML
    public void onAddBind() {
        KeyBinding newBind = new KeyBinding(List.of("Set Key"), "NewAction", "", true);
        bindsObservable.add(newBind);
        AppData.getInstance().setBinds(new ArrayList<>(bindsObservable));
    }

    @FXML
    public void onRemoveBind() throws IOException {
        ObservableList<KeyBinding> selectedItems = bindsTable.getSelectionModel().getSelectedItems();
        if (!selectedItems.isEmpty()) {
            CustomDialog.showDialog("Confirm Deletion", "Are you sure you want to delete the following items?",
                    stage -> {
                        bindsObservable.removeAll(selectedItems);
                        AppData.getInstance().setBinds(new ArrayList<>(bindsObservable));
                        stage.close();
                    },
                    vbox -> {
                        vbox.setSpacing(15);
                        vbox.setPadding(new Insets(10));
                        vbox.setAlignment(Pos.CENTER);

                        VBox itemsContainer = new VBox(5);
                        itemsContainer.setAlignment(Pos.TOP_LEFT);

                        for (KeyBinding kb : selectedItems) {
                            Label label = new Label(kb.getKeyDisplay() + " - " + kb.getActionType() + " - " + kb.getContext());
                            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                            label.setPadding(new Insets(2));
                            itemsContainer.getChildren().add(label);
                        }

                        ScrollPane scrollPane = new ScrollPane(itemsContainer);
                        scrollPane.setFitToWidth(true);
                        scrollPane.setPrefViewportHeight(200);
                        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

                        vbox.getChildren().add(scrollPane);
                    }
            );
        }
    }

    private void onAssignKey(KeyBinding bind) throws IOException {
        Set<String> currentPressed = new HashSet<>();
        AtomicReference<TextField> keyField = new AtomicReference<>(new TextField());

        CustomDialog.showDialog("Assign Key", "Press the key combination you want",
                stage -> {
                    String text = keyField.get().getText();
                    if (text != null && !text.isEmpty()) {
                        List<String> keys = List.of(text.split("\\+"));
                        bind.setKeys(new ArrayList<>(keys));
                    }
                    bindsTable.refresh();
                    AppData.getInstance().setBinds(new ArrayList<>(bindsObservable));
                    stage.close();
                },
                vbox -> {
                    vbox.setSpacing(10);
                    vbox.setPadding(new Insets(15));
                    vbox.setAlignment(Pos.CENTER);

                    keyField.set(new TextField(bind.getKeyDisplay()));
                    keyField.get().setAlignment(Pos.CENTER);
                    keyField.get().setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                    keyField.get().setEditable(false);

                    vbox.getChildren().add(keyField.get());


                    keyField.get().setOnKeyPressed(event -> {
                        currentPressed.add(event.getCode().getName());
                        keyField.get().setText(String.join("+", currentPressed));
                    });
                    keyField.get().setOnKeyReleased(event -> {
                        currentPressed.remove(event.getCode().getName());
                    });

                    keyField.get().setOnMouseClicked(e -> keyField.get().requestFocus());
                    keyField.get().requestFocus();
                });
    }
}