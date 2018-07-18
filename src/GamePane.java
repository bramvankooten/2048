import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;

/**
 * (File info).
 *
 * @author Bram
 */
public class GamePane extends StackPane {

    private GameManager gameManager;

    public GamePane() {
        gameManager = new GameManager();
        getChildren().add(gameManager);
        addKeyHandler(this);
        setFocusTraversable(true);
        this.setOnMouseClicked(e -> requestFocus());
    }

    private void addKeyHandler(Node node) {
        node.setOnKeyPressed(key -> {
            KeyCode keyCode = key.getCode();
            if (keyCode.isArrowKey()) {
                Direction direction = Direction.valueFor(keyCode);
                move(direction);
            }
        });
    }

    private void move(Direction direction) {
        gameManager.move(direction);
    }
}
