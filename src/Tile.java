import javafx.geometry.Pos;
import javafx.scene.control.Label;

import java.util.Optional;
import java.util.Random;

/**
 * (File info).
 *
 * @author Bram
 */
public class Tile extends Label {

    private Integer value;
    private Location location;
    private Boolean merged;

    public Tile(Integer value) {
        final int squareSize = Board.CELL_SIZE - 13;
        setMinSize(squareSize, squareSize);
        setMaxSize(squareSize, squareSize);
        setPrefSize(squareSize, squareSize);
        setAlignment(Pos.CENTER);
        this.value = value;
        this.merged = false;
        setText(value.toString());
        getStyleClass().addAll("game-label");
    }

    public static Tile newRandomTile() {
        int value = new Random().nextDouble() < 0.9 ? 2 : 4;
        return new Tile(value);
    }

    public Integer getValue() {
        return value;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isMerged() {
        return merged;
    }

    public void clearMerge() {
        merged = false;
    }

    public boolean isMergable(Optional<Tile> anotherTile) {
        return anotherTile.filter(t -> t.getValue().equals(getValue())).isPresent();
    }

    public void merge(Tile other) {
        this.value += other.getValue();
        setText(value.toString());
        merged = true;
    }

    @Override
    public String toString() {
        return "Tile{" + "value=" + value + ", location=" + location + '}';
    }
}
