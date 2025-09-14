package ui;

import core.data.AppData;
import core.data.ConsoleLine;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.paint.Color;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

public class PageConsoleController implements Initializable {

    @FXML
    private TextFlow consoleTextFlow;

    @FXML
    private ScrollPane consoleScrollPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadConsoleFromAppData();
    }

    private void loadConsoleFromAppData() {
        Platform.runLater(() -> {
            consoleTextFlow.getChildren().clear();
            for (ConsoleLine line : AppData.getInstance().getConsoleLines()) {
                Text t = new Text(line.getText());
                t.setFill(line.getColor());
                consoleTextFlow.getChildren().add(t);
            }

            consoleScrollPane.layout();
            consoleScrollPane.setVvalue(1.0);
        });
    }
}