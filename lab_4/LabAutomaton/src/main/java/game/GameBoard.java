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
import java.util.concurrent.ThreadLocalRandom;

public class GameBoard {
    private static final int PADDING = 0;
    private static final int CELL_SIZE = 20;
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

    private Cell[][] cells;
    private Group canvas;
    private Properties gameProperties;

    private int size;

    private final int fertility;
    private final int radDisp;
    private final int radBreed;
    private final boolean isSexual;
    private final int time;
    private ExecutorService executorService;

    public GameBoard(Group canvas, Properties gameProperties) {
        this.canvas = canvas;
        this.gameProperties = gameProperties;

        size = Integer.parseInt(this.gameProperties.getProperty("size"));
        fertility = Integer.parseInt(gameProperties.getProperty("fertility"));
        radDisp = Integer.parseInt(gameProperties.getProperty("rad_disp"));
        radBreed = Integer.parseInt(gameProperties.getProperty("rad_breed"));
        isSexual = gameProperties.getProperty("sex_breed").equals("sexual");
        time = Integer.parseInt(gameProperties.getProperty("time"));

        cells = new Cell[size][size];

        executorService = Executors.newFixedThreadPool(THREADS);

        drawGrid();
        settle();
    }

    private void drawGrid() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Rectangle rect = new Rectangle();

                rect.setX(i * (CELL_SIZE + PADDING));
                rect.setY(j * (CELL_SIZE + PADDING));
                rect.setWidth(CELL_SIZE);
                rect.setHeight(CELL_SIZE);
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
                cells[y][x] = cell;
                displayView(cell);
            }
        }
    }

    public void tick() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            if (isSexual) {
                sexualReproductionTick();
            } else {
                asexualReproductionTick();
            }
        }));
        timeline.setCycleCount(time);
        timeline.play();
    }

    private void asexualReproductionTick() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (cells[i][j] != null) {
                    int finalI = i;
                    int finalJ = j;
                    executorService.submit(() -> {
                        Cell cell = cells[finalI][finalJ];
                        populateRadius(cell);
                        removeCellAtPosition(cell.getPosition()); // don't care if someone has removed it
                        clearView(cell);
                    });
                }
            }
        }
    }

    private void sexualReproductionTick() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (cells[i][j] != null) {
                    int finalI = i;
                    int finalJ = j;
                    executorService.submit(() -> {
                        Cell cell = cells[finalI][finalJ];
                        Cell partner = findPartner(cell, radBreed);
                        if (partner != null) {
                            populateRadius(cell);
                            removeCellAtPosition(partner.getPosition()); // don't care if someone has removed it
                            clearView(partner);
                            removeCellAtPosition(cell.getPosition());
                            clearView(cell);
                        }
                    });
                }
            }
        }
    }

    private void displayView(Cell cell) {
        Platform.runLater(() -> {
            Position position = cell.getPosition();
            Rectangle rectangle = (Rectangle) canvas.getChildren().get(position.getY() * size + position.getX());
            rectangle.setFill(COLORS[cell.getGroup()]);
        });
    }

    private void clearView(Cell cell) {
        Platform.runLater(() -> {
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

            synchronized (this) {
                if (cells[parent.getPosition().getY()][parent.getPosition().getX()] != parent) {
                    break;
                }
                Cell competitorCell = cells[newCell.getPosition().getY()][newCell.getPosition().getX()];

                if (competitorCell != null) {
                    if (ThreadLocalRandom.current().nextBoolean()) { // probability 50%
                        cells[newCell.getPosition().getY()][newCell.getPosition().getX()] = newCell;
                        displayView(newCell);
                    }
                } else {
                    cells[newCell.getPosition().getY()][newCell.getPosition().getX()] = newCell;
                    displayView(newCell);
                }
            }
        }
    }

    private void removeCellAtPosition(Position position) {
        cells[position.getY()][position.getX()] = null;
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
                Cell partner = cells[y][x];
                if (partner != null && partner.getGroup() == cell.getGroup()) {
                    return partner;
                }
            }
        }
        return null;
    }
}
