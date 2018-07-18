import javafx.scene.Group;
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
    private static final int GRID_SIZE = 4;

    private final Group gridGroup = new Group();

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
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
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
//        tile.setScaleX(0);
//        tile.setScaleY(0);

        gridGroup.getChildren().add(tile);

        return tile;
    }

    public Group getGridGroup() {
        return gridGroup;
    }
}
