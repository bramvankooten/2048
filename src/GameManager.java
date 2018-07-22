import javafx.animation.*;
import javafx.scene.Group;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * (File info).
 *
 * @author Bram
 */
public class GameManager extends Group {

    private static final Duration ANIMATION_EXISTING_TILE = Duration.millis(65);
    private static final Duration ANIMATION_NEWLY_ADDED_TILE = Duration.millis(125);
    private static final Duration ANIMATION_MERGED_TILE = Duration.millis(80);

    private final int gridSize;

    private volatile boolean movingTiles = false;
    private final List<Location> locations = new ArrayList<>();
    private final Map<Location, Tile> gameGrid;
    private final Set<Tile> mergedToBeRemoved = new HashSet<>();

    private final Board board;
    private final GridOperator gridOperator;

    public GameManager() {
        this(GridOperator.DEFAULT_GRID_SIZE);
    }

    public GameManager(int gridSize) {
        this.gridSize = gridSize;
        this.gameGrid = new HashMap<>();
        gridOperator = new GridOperator(gridSize);
        board = new Board(gridOperator);
        this.getChildren().add(board);

        board.resetGameProperty().addListener((ov, b, b1) -> {
            if (b1) {
                startGame();
            }
        });
        board.clearGameProperty().addListener((ov, b, b1) -> {
            if (b1) {
                initializeGameGrid();
            }
        });

        initializeGameGrid();
        startGame();
    }

    public void initializeGameGrid() {
        gameGrid.clear();
        locations.clear();
        gridOperator.traverseGrid((x, y) -> {
            Location thisloc = new Location(x, y);
            locations.add(thisloc);
            gameGrid.put(thisloc, null);
            return 0;
        });
    }

    public void startGame() {
        Tile tile0 = Tile.newRandomTile();
        List<Location> randomLocs = new ArrayList<>(locations);
        Collections.shuffle(randomLocs);
        Iterator<Location> locs = randomLocs.stream().limit(2).iterator();
        tile0.setLocation(locs.next());

        Tile tile1 = null;
        if (new Random().nextFloat() <= 0.8) {
            tile1 = Tile.newRandomTile();
            if (tile1.getValue() == 4 && tile0.getValue() == 4) {
                tile1 = Tile.newTile(2);
            }
            tile1.setLocation(locs.next());
        }

        Arrays.asList(tile0, tile1).stream().filter(Objects::nonNull)
                .forEach(t -> gameGrid.put(t.getLocation(), t));

        redrawTilesInGameGrid();

    }

    private void redrawTilesInGameGrid() {
        gameGrid.values().stream().filter(Objects::nonNull).forEach(board::addTile);
    }

    public void moveTiles(Direction direction) {
        synchronized (gameGrid) {
            if (movingTiles) {
                return;
            }
        }

        board.setGameOver(true);

        mergedToBeRemoved.clear();
        ParallelTransition parallelTransition = new ParallelTransition();
        gridOperator.sortGrid(direction);
        final int tilesWereMoved = gridOperator.traverseGrid((x, y) -> {
            AtomicInteger merged = new AtomicInteger();
            Location thisLoc = new Location(x, y);
            Optional<Tile> optionalTile = optionalTile(thisLoc);
            if (optionalTile.isPresent()) {
                Location farthest = findFarthestLocation(thisLoc, direction);
                Location nextLoc = farthest.offset(direction);
                optionalTile(nextLoc).filter(t -> t.isMergable(optionalTile) && !t.isMerged())
                        .ifPresent(t -> {
                            Tile tile = optionalTile.get();
                            t.merge(tile);
                            t.toFront();
                            gameGrid.put(nextLoc, t);
                            gameGrid.replace(thisLoc, null);

                            parallelTransition.getChildren().add(animateExistingTile(tile, t.getLocation()));
                            parallelTransition.getChildren().add(animateMergedTile(t));
                            mergedToBeRemoved.add(tile);

                            merged.set(1);
                        });

                if (merged.get() == 0 && farthest.isValidFor(gridSize) && !farthest.equals(thisLoc)) {
                    Tile tile = optionalTile.get();
                    tile.setLocation(farthest);
                    gameGrid.put(farthest, optionalTile.get());
                    gameGrid.replace(thisLoc, null);
                    parallelTransition.getChildren().add(animateExistingTile(optionalTile.get(), farthest));
                    merged.set(1);
                }
            }
            return merged.get();
        });

        if (parallelTransition.getChildren().size() > 0) {
            parallelTransition.setOnFinished(e -> {
                board.getGridGroup().getChildren().removeAll(mergedToBeRemoved);

                gameGrid.values().stream().filter(Objects::nonNull).forEach(Tile::clearMerge);

                Location randomAvailable = findRandomAvailableLocation();
                if (randomAvailable == null && mergeMovementsAvailable() == 0) {
                    board.setGameOver(true);
                } else if (randomAvailable != null && tilesWereMoved > 0) {
                    synchronized (gameGrid) {
                        movingTiles = false;
                    }
                    addAndAnimateRandomTile(randomAvailable);
                }
            });

            synchronized (gameGrid) {
                movingTiles = true;
            }

            parallelTransition.play();
        }

    }

    private Optional<Tile> optionalTile(Location loc) {
        return Optional.ofNullable(gameGrid.get(loc));
    }

    private Location findFarthestLocation(Location location, Direction direction) {
        Location farthest;

        do {
            farthest = location;
            location = farthest.offset(direction);
        } while (gridOperator.isValidLocation(location) && !optionalTile(location).isPresent());

        return farthest;
    }

    public void move(Direction direction) {
        if (!board.isLayerOn().get()) {
            moveTiles(direction);
        }
    }

    private int mergeMovementsAvailable() {
        final AtomicInteger pairsOfMergableTiles = new AtomicInteger();

        Stream.of(Direction.UP, Direction.LEFT).parallel().forEach(direction -> {
            gridOperator.traverseGrid((x, y) -> {
                Location thisLoc = new Location(x, y);
                optionalTile(thisLoc).ifPresent(t -> {
                    if (t.isMergable(optionalTile(thisLoc.offset(direction)))) {
                        pairsOfMergableTiles.incrementAndGet();
                    }
                });
                return 0;
            });
        });

        return pairsOfMergableTiles.get();
    }

    private Location findRandomAvailableLocation() {
        List<Location> availableLocations = locations.stream().filter(l -> gameGrid.get(l) == null)
                .collect(Collectors.toList());

        if (availableLocations.isEmpty()) {
            return null;
        }

        Collections.shuffle(availableLocations);
        return availableLocations.get(0);
    }

    private Timeline animateExistingTile(Tile tile, Location newLocation) {
        Timeline timeline = new Timeline();
        KeyValue kvX = new KeyValue(tile.layoutXProperty(), newLocation.getLayoutX(Board.CELL_SIZE) - (tile.getMinHeight() / 2), Interpolator.EASE_OUT);
        KeyValue kvY = new KeyValue(tile.layoutYProperty(), newLocation.getLayoutY(Board.CELL_SIZE) - (tile.getMinHeight() / 2), Interpolator.EASE_OUT);

        KeyFrame kfX = new KeyFrame(ANIMATION_EXISTING_TILE, kvX);
        KeyFrame kfY = new KeyFrame(ANIMATION_EXISTING_TILE, kvY);

        timeline.getKeyFrames().addAll(kfX, kfY);

        return timeline;
    }

    private SequentialTransition animateMergedTile(Tile tile) {
        final ScaleTransition scale0 = new ScaleTransition(ANIMATION_MERGED_TILE, tile);
        scale0.setToX(1.2);
        scale0.setToY(1.2);
        scale0.setInterpolator(Interpolator.EASE_IN);

        final ScaleTransition scale1 = new ScaleTransition(ANIMATION_MERGED_TILE, tile);
        scale1.setToX(1.0);
        scale1.setToY(1.0);
        scale1.setInterpolator(Interpolator.EASE_OUT);

        return new SequentialTransition(scale0, scale1);
    }

    private void addAndAnimateRandomTile(Location randomLocation) {
        Tile tile = board.addRandomTile(randomLocation);
        gameGrid.put(tile.getLocation(), tile);

        animateNewlyAddedTile(tile).play();
    }

    private ScaleTransition animateNewlyAddedTile(Tile tile) {
        final ScaleTransition scaleTransition = new ScaleTransition(ANIMATION_NEWLY_ADDED_TILE, tile);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);
        scaleTransition.setOnFinished(e -> {
            if (this.gameGrid.values().parallelStream().noneMatch(Objects::isNull) && mergeMovementsAvailable() == 0) {
                board.setGameOver(true);
            }
        });
        return scaleTransition;
    }
}
