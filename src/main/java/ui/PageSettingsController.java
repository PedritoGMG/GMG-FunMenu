package ui;

import core.Main;
import core.data.AppData;
import core.util.Toast;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class PageSettingsController implements Initializable {

    @FXML
    private VBox adminContainer, banContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppData.getInstance().getAdminUsers().forEach(this::addAdminField);
        AppData.getInstance().getBannedUsers().forEach(this::addBanField);
    }

    private void showAddUserDialog(String title, String message, java.util.function.Function<String, Boolean> addAction, Consumer<String> addFieldAction) throws IOException {
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
}
