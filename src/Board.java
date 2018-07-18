import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * (File info).
 *
 * @author Bram
 */
public class Board extends Group {
    public static final int CELL_SIZE = 128;
    private static final int BORDER_WIDTH = 8;

    private final Group gridGroup = new Group();

    private final HBox overlay = new HBox();
    private final VBox txtOverlay = new VBox(10);
    private final Label lOvrText= new Label();
    private final Label lOvrSubText= new Label();
    private final HBox buttonsOverlay = new HBox();
    private final Button bTry = new Button("Try again");
    private final Button bContinue = new Button("Keep going");
    private final Button bContinueNo = new Button("No, keep going");
    private final Button bSave = new Button("Save");
    private final Button bRestore = new Button("Restore");
    private final Button bQuit = new Button("Quit");

    private final GridOperator gridOperator;

    public Board(GridOperator gridOperator) {
        this.gridOperator = gridOperator;
        createGrid();
        getChildren().add(gridGroup);
    }

    private Rectangle createCell(int i, int j) {
        final double arcSize = CELL_SIZE / 6d;
        Rectangle cell = new Rectangle(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        cell.setFill(Color.TRANSPARENT);
        cell.setStroke(Color.GRAY);
        cell.setArcHeight(arcSize);
        cell.setArcWidth(arcSize);
        return cell;
    }

    private void createGrid() {
        for (int i = 0; i < gridOperator.getGridSize(); i++) {
            for (int j = 0; j < gridOperator.getGridSize(); j++) {
                gridGroup.getChildren().add(createCell(i, j));
            }
        }

        gridGroup.setManaged(false);
    }

    public void addTile(Tile tile) {
        double layoutX = tile.getLocation().getLayoutX(CELL_SIZE) - (tile.getMinWidth() / 2);
        double layoutY = tile.getLocation().getLayoutY(CELL_SIZE) - (tile.getMinHeight() / 2);

        tile.setLayoutX(layoutX);
        tile.setLayoutY(layoutY);

        gridGroup.getChildren().add(tile);
    }

    public Tile addRandomTile(Location randomLocation) {
        Tile tile = Tile.newRandomTile();
        tile.setLocation(randomLocation);

        double layoutX = tile.getLocation().getLayoutX(CELL_SIZE) - (tile.getMinWidth() / 2);
        double layoutY = tile.getLocation().getLayoutY(CELL_SIZE) - (tile.getMinHeight() / 2);

        tile.setLayoutX(layoutX);
        tile.setLayoutY(layoutY);
        tile.setScaleX(0);
        tile.setScaleY(0);

        gridGroup.getChildren().add(tile);

        return tile;
    }

    public Group getGridGroup() {
        return gridGroup;
    }

    private class Overlay implements ChangeListener<Boolean> {

        private final Button btn1, btn2;
        private final String message, warning;
        private final String style1, style2;
        private final boolean pause;

        public Overlay(String message, String warning, Button btn1, Button btn2, String style1, String style2, boolean pause) {
            this.message = message;
            this.warning = warning;
            this.btn1 = btn1;
            this.btn2 = btn2;
            this.style1 = style1;
            this.style2 = style2;
            this.pause = pause;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (newValue) {
                overlay.getStyleClass().setAll("game-overlay", style1);
                lOvrText.setText(message);
                lOvrText.getStyleClass().setAll("game-label", style2);
                lOvrSubText.setText(warning);
                lOvrSubText.getStyleClass().setAll("game-label", "game-lblWarning");
                txtOverlay.getChildren().setAll(lOvrText, lOvrSubText);
                buttonsOverlay.getChildren().setAll(btn1);
                if (btn2 != null) {
                    buttonsOverlay.getChildren().add(btn2);
                }
            }
        }
    }
}
