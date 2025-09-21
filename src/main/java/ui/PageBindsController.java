package ui;

import core.data.AppData;
import core.keybindings.KeyBinding;
import core.triggers.AudioTrigger;
import core.triggers.TriggerFactory;
import core.util.FileSelector;
import core.util.HoverAnimator;
import core.util.Toast;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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

        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyColumn.setCellFactory(col -> new TableCell<>() {
            private final Button button = new Button();

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
                    comboBox.setItems(FXCollections.observableArrayList(TriggerFactory.getAllTriggers().keySet()));
                    comboBox.setValue(actionType);
                    comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                        KeyBinding bind = getTableView().getItems().get(getIndex());
                        bind.setActionType(newVal);
                        AppData.getInstance().setBinds(new ArrayList<>(bindsObservable));
                    });
                    setGraphic(comboBox);
                }
            }
        });

        contextColumn.setCellValueFactory(new PropertyValueFactory<>("context"));
        contextColumn.setCellFactory(col -> new TableCell<>() {
            private final TextField textField = new TextField();

            @Override
            protected void updateItem(String context, boolean empty) {
                super.updateItem(context, empty);
                if (empty || context == null) {
                    setGraphic(null);
                } else {
                    textField.setText(context);
                    textField.textProperty().addListener((obs, oldVal, newVal) -> {
                        KeyBinding bind = getTableView().getItems().get(getIndex());
                        bind.setContext(newVal);
                        AppData.getInstance().setBinds(new ArrayList<>(bindsObservable));
                    });
                    setGraphic(textField);
                }
            }
        });
    }

    @FXML
    public void onAddBind() {
        KeyBinding newBind = new KeyBinding("NewKey", "NewAction", "Context", true);
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
                            Label label = new Label(kb.getKey() + " - " + kb.getActionType() + " - " + kb.getContext());
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
        final String[] selectedKey = {bind.getKey()};

        CustomDialog.showDialog("Assign Key", "Press the key you want to assign",
                stage -> {
                    bind.setKey(selectedKey[0]);
                    bindsTable.refresh();
                    AppData.getInstance().setBinds(new ArrayList<>(bindsObservable));
                    stage.close();
                },
                vbox -> {
                    vbox.setSpacing(10);
                    vbox.setPadding(new Insets(15));
                    vbox.setAlignment(Pos.CENTER);

                    TextField keyField = new TextField(bind.getKey());
                    keyField.setAlignment(Pos.CENTER);
                    keyField.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                    keyField.setEditable(false);

                    vbox.getChildren().add(keyField);

                    final String[] tempKey = { bind.getKey() };

                    keyField.setOnKeyPressed(event -> {
                        tempKey[0] = event.getCode().getName();
                        selectedKey[0] = tempKey[0];
                        keyField.setText(selectedKey[0]);
                    });

                    keyField.setOnMouseClicked(e -> keyField.requestFocus());
                    keyField.requestFocus();
                });
    }
}