package core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args); // Esto lanza la aplicación JavaFX
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ui/main.fxml")); // Carga el FXML
        Scene scene = new Scene(root);
        
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
		
        stage.setTitle("PGMG-FunPad");
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        
        stage.setScene(scene);
        stage.show();
    }
}