package game;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
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
    private static final int THREADS = 4;
    private static final int TICK_DURATION = 100;

    private ArrayList<AtomicReferenceArray<Cell>> cells;
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

        cells = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            cells.add(new AtomicReferenceArray<>(size));
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

                if (cells.get(y).get(x) == null) {
                    Position position = new Position(x, y);
                    Cell cell = new Cell(position, group);
                    put(cell);
                    displayCellView(cell);
                } else {
                    i--; // repeat process
                }
            }
        }
    }

    protected void put(Cell cell) {
        cells.get(cell.getY()).set(cell.getX(), cell);
    }

    protected Cell get(int x, int y) {
        return cells.get(y).get(x);
    }

    public void resettle() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells.get(i).set(j, null);
                Rectangle rectangle = (Rectangle) canvas.getChildren().get(i * size + j);
                rectangle.setFill(Color.TRANSPARENT);
            }
        }

        settle();
    }

    protected void asexualReproductionTick() {
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

    protected void sexualReproductionTick() {
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
                        Cell cell = cellsCopy[finalI][finalJ], partner;
                        synchronized (this) {
                            partner = findPartner(cellsCopy, cell, radBreed);
                            if (partner == null) return;
                            if (!cell.lock()) return;
                            if (!partner.lock()) {
                                cell.unlock();
                                return;
                            }
                        }

                        if (cell.isLocked() && partner.isLocked()) {
                            removeCell(cell);
                            removeCell(partner);
                            populateRadius(cell);
                        }
                    });
                }
            }
        }
    }

    private void displayCellView(Cell cell) {
        Platform.runLater(() -> {
            if (cell == null) return;
            Position position = cell.getPosition();
            Rectangle rectangle = (Rectangle) canvas.getChildren().get(position.getX() * size + position.getY());
            rectangle.setFill(COLORS[cell.getGroup()]);
        });
    }

    private void clearCellView(Cell cell) {
        Platform.runLater(() -> {
            if (cell == null) return;
            Position position = cell.getPosition();
            Rectangle rectangle = (Rectangle) canvas.getChildren().get(position.getX() * size + position.getY());
            rectangle.setFill(Color.TRANSPARENT);
        });
    }

    protected void populateRadius(Cell parent) {
        ArrayList<Cell> excluded = new ArrayList<>(fertility);
        for (int i = 0; i < fertility; i++) {

            Cell newCell = createCell(parent, radDisp, excluded);
            if (newCell == null) continue;

            newCell = cells.get(newCell.getY()).accumulateAndGet(newCell.getX(), newCell, (owner, claimant) -> {
                // must be side-effect free
                if (owner == null) {
                    return claimant;
                }
                if (ThreadLocalRandom.current().nextBoolean()) { // probability 50%
                    return claimant;
                }
                return owner;
            });

            if (newCell.getGroup() == parent.getGroup()) {
                excluded.add(newCell);
            }

            displayCellView(cells.get(newCell.getY()).get(newCell.getX()));
        }
    }

    private void removeCell(Cell cell) {
        if (cells.get(cell.getY()).compareAndSet(cell.getX(), cell, null)) {
            clearCellView(cell);
        }
    }

    private Cell createCell(Cell parent, int radius, ArrayList<Cell> exclude) {
        // returns null if no cell can be created

        Position parentPosition = parent.getPosition();

        int minX = Math.max(0, parentPosition.getX() - radius);
        int maxX = Math.min(size - 1, parentPosition.getX() + radius);
        int minY = Math.max(0, parentPosition.getY() - radius);
        int maxY = Math.min(size - 1, parentPosition.getY() + radius);

        if (exclude.size() >= (maxX - minX + 1) * (maxY - minY + 1) - 1) {
            return null;
        }

        final Position position = new Position(parentPosition.getX(), parentPosition.getY());

        do {
            position.setX(
                    ThreadLocalRandom.current().nextInt(minX, maxX + 1));
            position.setY(ThreadLocalRandom.current().nextInt(minY, maxY + 1));

        } while (parentPosition.equals(position) || exclude.stream().anyMatch(cell -> cell.getPosition().equals(position)));

        return new Cell(position, parent.getGroup());
    }

    private Cell findPartner(Cell[][] searchArea, Cell cell, int radius) {
        Position position = cell.getPosition();

        int minX = Math.max(0, position.getX() - radius);
        int maxX = Math.min(size - 1, position.getX() + radius);
        int minY = Math.max(0, position.getY() - radius);
        int maxY = Math.min(size - 1, position.getY() + radius);

        // random order of partner search
        // find better approach
        int possiblePartnerPositions = (maxX - minX + 1) * (maxY - minY + 1) - 1;
        Set<Position> alreadyChecked = new HashSet<>(possiblePartnerPositions);

        while (alreadyChecked.size() < possiblePartnerPositions) {
            int x = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
            int y = ThreadLocalRandom.current().nextInt(minY, maxY + 1);
            if (x == cell.getPosition().getX() && y == cell.getPosition().getY()) {
                continue;
            }
            Position partnerPosition = new Position(x, y);
            if (!alreadyChecked.contains(partnerPosition)) {
                Cell partner = searchArea[y][x];
                if (partner != null && partner.getGroup() == cell.getGroup()) {
                    return partner;
                }
                alreadyChecked.add(partnerPosition);
            }
        }
        return null;
    }

    public void play() {
        ticker.play();
    }
}
