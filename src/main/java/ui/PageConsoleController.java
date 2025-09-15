package ui;

import core.data.AppData;
import core.data.ConsoleLine;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.ResourceBundle;

public class PageConsoleController implements Initializable {

    @FXML
    private TextFlow consoleTextFlow;

    @FXML
    private ScrollPane consoleScrollPane;

    @FXML
    private CheckBox chkShowRegisteredLines;

    private int lastLineIndex = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chkShowRegisteredLines.setSelected(AppData.getInstance().isShowRegisteredLines());

        AppData.getInstance().getConsoleLines().forEach(this::addConsoleLine);

        AppData.getInstance().getConsoleLines().addListener((ListChangeListener<ConsoleLine>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (ConsoleLine line : change.getAddedSubList()) {
                        addConsoleLine(line);
                    }
                }
            }
        });
    }

    private void addConsoleLine(ConsoleLine line) {
        Platform.runLater(() -> {
            Text t = new Text(line.getText());
            t.setFill(line.getColor());
            consoleTextFlow.getChildren().add(t);

            int max_lines = AppData.getInstance().getMAX_LINES();

            if (consoleTextFlow.getChildren().size() > max_lines) {
                consoleTextFlow.getChildren().remove(0, consoleTextFlow.getChildren().size() - max_lines);
            }

            consoleScrollPane.layout();
            consoleScrollPane.setVvalue(1.0);
        });
    }

    @FXML
    private void onShowRegisteredLinesChanged() {
        AppData.getInstance().setShowRegisteredLines(chkShowRegisteredLines.isSelected());
    }
}