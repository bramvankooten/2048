/**
 * (File info).
 *
 * @author Bram
 */

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Game2048 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("2048");
        GamePane root = new GamePane();
        root.getChildren().add(new Board(null));
        Scene scene = new Scene(root);
        scene.getStylesheets().add("game.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
