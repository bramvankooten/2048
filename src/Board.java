import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
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
    private static final int TOP_HEIGHT = 92;
    private static final int GAP_HEIGHT = 50;

    /**
     * Properties
     */
    private final IntegerProperty gameScoreProperty = new SimpleIntegerProperty(0);
    private final BooleanProperty gameOverProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty layerOnProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty resetGame = new SimpleBooleanProperty(false);
    private final BooleanProperty clearGame = new SimpleBooleanProperty(false);


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

    private final VBox vgame = new VBox(0);
    private final Group gridGroup = new Group();
    private final GridOperator gridOperator;
    private final int gridWidth;

    public Board(GridOperator gridOperator) {
        this.gridOperator = gridOperator;
        gridWidth = CELL_SIZE * gridOperator.getGridSize() + BORDER_WIDTH * 2;

        getChildren().add(vgame);
        getChildren().add(gridGroup);
        createGrid();

        initGameProperties();
    }

    private Rectangle createCell(int i, int j) {
        final double arcSize = CELL_SIZE / 6d;
        Rectangle cell = new Rectangle(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        cell.setFill(Color.TRANSPARENT);
        cell.setStroke(Color.GRAY);
        cell.setArcHeight(arcSize);
        cell.setArcWidth(arcSize);
        cell.getStyleClass().add("game-grid-cell");
        return cell;
    }

    private void createGrid() {
        gridOperator.traverseGrid((i, j) -> {
            gridGroup.getChildren().add(createCell(i, j));
            return 0;
        });

        gridGroup.getStyleClass().add("game-grid");
        gridGroup.setManaged(false);

        HBox hBottom = new HBox();
        hBottom.getStyleClass().add("game-backGrid");
        hBottom.setMinSize(gridWidth, gridWidth);
        hBottom.setPrefSize(gridWidth, gridWidth);
        hBottom.setMaxSize(gridWidth, gridWidth);
        hBottom.toBack();

        // Clip hBottom to keep the dropshadow effects within the hBottom
        Rectangle rect = new Rectangle(gridWidth, gridWidth);
        hBottom.setClip(rect);
        hBottom.getChildren().add(gridGroup);

        vgame.getChildren().add(hBottom);
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

    private void initGameProperties() {

        overlay.setMinSize(gridWidth, gridWidth);
        overlay.setAlignment(Pos.CENTER);
        //overlay.setTranslateY(TOP_HEIGHT + GAP_HEIGHT);

        overlay.getChildren().setAll(txtOverlay);
        txtOverlay.setAlignment(Pos.CENTER);

        buttonsOverlay.setAlignment(Pos.CENTER);
        //buttonsOverlay.setTranslateY(TOP_HEIGHT + GAP_HEIGHT + gridWidth / 2);
        buttonsOverlay.setTranslateY(gridWidth / 2);
        buttonsOverlay.setMinSize(gridWidth, gridWidth / 2);
        buttonsOverlay.setSpacing(10);

        bTry.getStyleClass().add("game-button");
        bTry.setOnAction(e -> btnTryAgain());

        gameOverProperty.addListener(new Overlay("Game over!", "", bTry, null, "game-overlay-over", "game-lblOver", false));

        layerOnProperty.addListener((ov, b, b1) -> {
            if (!b1) {
                getChildren().removeAll(overlay, buttonsOverlay);
                getParent().requestFocus();
            } else {
                buttonsOverlay.getChildren().get(0).requestFocus();
            }
        });
    }
    
    private void btnTryAgain() {
        doResetGame();
    }

    private void doResetGame() {
        doClearGame();
        resetGame.set(true);
    }

    private void doClearGame() {
        gridGroup.getChildren().removeIf(c -> c instanceof Tile);
        getChildren().removeAll(overlay, buttonsOverlay);

        clearGame.set(false);
        resetGame.set(false);
        gameScoreProperty.set(0);
        gameOverProperty.set(false);
        layerOnProperty.set(false);

        clearGame.set(true);
    }

    public BooleanProperty resetGameProperty() {
        return resetGame;
    }

    public BooleanProperty clearGameProperty() {
        return clearGame;
    }

    public BooleanProperty isLayerOn() {
        return layerOnProperty;
    }

    public void setGameOver(boolean gameOver) {
        gameOverProperty.set(gameOver);
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
                if (!layerOnProperty.get()) {
                    Board.this.getChildren().addAll(overlay, buttonsOverlay);
                    layerOnProperty.set(true);
                }
            }
        }
    }
}
