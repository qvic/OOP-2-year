package game;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class GameBoard {
    private static final int PADDING = 0;
    private static final int CANVAS_SIZE = 600;
    private static final double STROKE_WIDTH = 1.0;
    private static final Color STROKE_COLOR = Color.BLACK;
    private static final Color[] COLORS = {
            Color.rgb(52, 152, 219),
            Color.rgb(231, 76, 60),
            Color.rgb(241, 196, 15),
            Color.rgb(26, 188, 156),
            Color.rgb(155, 89, 182)
    };
    public static final int THREADS = 4;
    public static final int TICK_DURATION = 200;

    private AtomicReferenceArray<AtomicReferenceArray<Cell>> cells;
    private Group canvas;
    private Properties gameProperties;
    private final Timeline ticker;

    private final int size;

    private final int fertility;
    private final int radDisp;
    private final int radBreed;
    private final boolean isSexual;
    private final int time;
    private final ExecutorService executorService;

    public GameBoard(Group canvas, Properties gameProperties) {
        this.canvas = canvas;
        this.gameProperties = gameProperties;

        size = Integer.parseInt(this.gameProperties.getProperty("size"));
        fertility = Integer.parseInt(gameProperties.getProperty("fertility"));
        radDisp = Integer.parseInt(gameProperties.getProperty("rad_disp"));
        radBreed = Integer.parseInt(gameProperties.getProperty("rad_breed"));
        isSexual = gameProperties.getProperty("sex_breed").equals("sexual");
        time = Integer.parseInt(gameProperties.getProperty("time"));

        cells = new AtomicReferenceArray<>(size);
        for (int i = 0; i < size; i++) {
            cells.set(i, new AtomicReferenceArray<>(size));
        }

        final ThreadFactory threadFactory = new ThreadFactory() {
            private int counter = 0;

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "CellWorker-" + counter);
                counter++;
                thread.setDaemon(true);
                return thread;
            }
        };

        executorService = Executors.newFixedThreadPool(THREADS, threadFactory);

        ticker = new Timeline(new KeyFrame(Duration.millis(TICK_DURATION), event -> {
            if (isSexual) {
                sexualReproductionTick();
            } else {
                asexualReproductionTick();
            }
        }));
        ticker.setCycleCount(time);

        drawGrid(CANVAS_SIZE / size);
        settle();
    }

    private void drawGrid(int cellSize) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Rectangle rect = new Rectangle();

                rect.setX(i * (cellSize + PADDING));
                rect.setY(j * (cellSize + PADDING));
                rect.setWidth(cellSize);
                rect.setHeight(cellSize);
                rect.setStrokeWidth(STROKE_WIDTH);
                rect.setStroke(STROKE_COLOR);
                rect.setFill(Color.TRANSPARENT);

                canvas.getChildren().add(rect);
            }
        }
    }

    public void settle() {
        double density = Double.parseDouble(gameProperties.getProperty("density"));
        int numberOfGroups = Integer.parseInt(gameProperties.getProperty("no_of_spp"));

        int groupSize = (int) (size * size * density / numberOfGroups);

        assert numberOfGroups < 6;

        for (int group = 0; group < numberOfGroups; group++) {
            for (int i = 0; i < groupSize; i++) {
                int x = ThreadLocalRandom.current().nextInt(size);
                int y = ThreadLocalRandom.current().nextInt(size);

                Position position = new Position(x, y);
                Cell cell = new Cell(position, group);
                cells.get(y).set(x, cell);
                displayCellView(cell);
            }
        }
    }

    public void play() {
        ticker.play();
    }

    private void asexualReproductionTick() {
        Cell[][] cellsCopy = new Cell[size][size];
        for (int i = 0; i < size; i++) { // find better approach
            for (int j = 0; j < size; j++) {
                cellsCopy[i][j] = cells.get(i).get(j);
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (cellsCopy[i][j] != null) {
                    int finalI = i;
                    int finalJ = j;
                    executorService.submit(() -> {
                        Cell cell = cellsCopy[finalI][finalJ];
                        populateRadius(cell);
                        removeCell(cell);
                    });
                }
            }
        }
    }

    private void sexualReproductionTick() {
//        Cell[][] allCells = new Cell[size][size];
//        for (int i = 0; i < size; i++) {
//            allCells[i] = new Cell[size];
//            System.arraycopy(cells[i], 0, allCells[i], 0, size);
//        }
//
//        for (int i = 0; i < size; i++) {
//            for (int j = 0; j < size; j++) {
//                if (cells[i][j] != null) {
//                    int finalI = i;
//                    int finalJ = j;
//                    executorService.submit(() -> {
//                        Cell cell = cells[finalI][finalJ];
//                        Cell partner = findPartner(cell, radBreed);
//                        if (partner != null) {
//                            populateRadius(cell);
//                            removeCell(partner.getPosition()); // don't care if someone has removed it
//                            clearCellView(partner);
//                            removeCell(cell.getPosition());
//                            clearCellView(cell);
//                        }
//                    });
//                }
//            }
//        }
    }

    private void displayCellView(Cell cell) {
        Platform.runLater(() -> {
            if (cell == null) return;
            Position position = cell.getPosition();
            Rectangle rectangle = (Rectangle) canvas.getChildren().get(position.getY() * size + position.getX());
            rectangle.setFill(COLORS[cell.getGroup()]);
        });
    }

    private void clearCellView(Cell cell) {
        Platform.runLater(() -> {
            if (cell == null) return;
            Position position = cell.getPosition();
            Rectangle rectangle = (Rectangle) canvas.getChildren().get(position.getY() * size + position.getX());
            rectangle.setFill(Color.TRANSPARENT);
        });
    }

    private Cell createCell(Cell parent, int radius) {
        Position position = parent.getPosition();

        int minX = Math.max(0, position.getX() - radius);
        int maxX = Math.min(size - 1, position.getX() + radius);
        int minY = Math.max(0, position.getY() - radius);
        int maxY = Math.min(size - 1, position.getY() + radius);

        int x, y;

        do {
            x = minX + ThreadLocalRandom.current().nextInt(maxX - minX + 1);
            y = minY + ThreadLocalRandom.current().nextInt(maxY - minY + 1);
        } while (x == position.getX() && y == position.getY());

        return new Cell(new Position(x, y), parent.getGroup());
    }

    private void populateRadius(Cell parent) {
        for (int i = 0; i < fertility; i++) {

            Cell newCell = createCell(parent, radDisp);

            cells.get(newCell.getY()).accumulateAndGet(newCell.getX(), newCell, (owner, claimant) -> {
                // must be side-effect free
                if (owner == null) {
                    return claimant;
                }
                if (ThreadLocalRandom.current().nextBoolean()) { // probability 50%
                    return claimant;
                }
                return owner;
            });

            displayCellView(cells.get(newCell.getY()).get(newCell.getX()));
        }
    }

    private void removeCell(Cell cell) {
        if (cells.get(cell.getY()).compareAndSet(cell.getX(), cell, null)) {
            clearCellView(cell);
        }
    }

    private Cell findPartner(Cell cell, int radius) {
        Position position = cell.getPosition();

        int minX = Math.max(0, position.getX() - radius);
        int maxX = Math.min(size - 1, position.getX() + radius);
        int minY = Math.max(0, position.getY() - radius);
        int maxY = Math.min(size - 1, position.getY() + radius);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (x == cell.getPosition().getX() && y == cell.getPosition().getY()) {
                    continue;
                }
                Cell partner = cells.get(y).get(x);
                if (partner != null && partner.getGroup() == cell.getGroup()) {
                    return partner;
                }
            }
        }
        return null;
    }
}
