package ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CustomDialog {
	
	private double xOffset = 0;
    private double yOffset = 0;
	
	@FXML
	private Stage dialogStage;
	
	@FXML
	private VBox vboxContainer;
	
	@FXML
	private Button btnDialogClose, btnDialogCancel;
	public Button btnDialogConfirm;
	
	@FXML
	private Label dialogHeaderText, dialogBodyText;
	
	public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
	
	public void setTitle(String text) {
		this.dialogHeaderText.setText(text);
    }
	
	public void setMessage(String text) {
		this.dialogBodyText.setText(text);
    }
	
	@FXML
	public void onClose() {
		dialogStage.close();
	}
	@FXML
	public void onCancel() {
		dialogStage.close();
	}
	@FXML
    private void onMousePressedTopBar(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void onMouseDraggedTopBar(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }
	
	public static void showDialog(String title, String message, Consumer<Stage> onConfirmar, Consumer<VBox> contenidoExtra) throws IOException {
	    FXMLLoader loader = new FXMLLoader(CustomDialog.class.getResource("/ui/dialog.fxml"));
	    Parent root = loader.load();

	    CustomDialog controller = loader.getController();

	    Stage dialogStage = new Stage();

	    Scene scene = new Scene(root);
	    scene.getStylesheets().add(CustomDialog.class.getResource("/ui/styles.css").toExternalForm());
	    scene.setFill(Color.TRANSPARENT);
	    
	    dialogStage.getIcons().add(new Image(CustomDialog.class.getResourceAsStream("/icon.png")));
	    dialogStage.setTitle("Dialog - GMG-FunMenu");

	    dialogStage.initModality(Modality.APPLICATION_MODAL);
	    dialogStage.initStyle(StageStyle.TRANSPARENT);

	    dialogStage.setScene(scene);
	    controller.setDialogStage(dialogStage);
	    
	    controller.setTitle(title);
	    controller.setMessage(message);
	    

	    if (onConfirmar != null) {
	        controller.btnDialogConfirm.setOnAction(e -> {
	        	onConfirmar.accept(dialogStage);
	        });
	    } else {
	        controller.btnDialogConfirm.setVisible(false);
			controller.btnDialogConfirm.setManaged(false);
	    }

	    if (contenidoExtra != null) {
	        contenidoExtra.accept(controller.vboxContainer);
	    }

	    dialogStage.showAndWait();
	}


}

