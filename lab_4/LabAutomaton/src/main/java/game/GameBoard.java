package game;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

public class GameBoard {
    private static final int PADDING = 0;
    private static final int CELL_SIZE = 30;
    private static final double STROKE_WIDTH = 2.0;
    private static final Color STROKE_COLOR = Color.BLACK;
    private static final Color[] COLORS = {
            Color.rgb(52, 152, 219),
            Color.rgb(231, 76, 60),
            Color.rgb(241, 196, 15),
            Color.rgb(26, 188, 156),
            Color.rgb(155, 89, 182)
    };

    private HashMap<Position, Cell> cells;
    private Group canvas;
    private Properties gameProperties;
    private Random random;

    private int size;

    private int fertility;
    private int radDisp;

    public GameBoard(Group canvas, Properties gameProperties) {
        this.canvas = canvas;
        this.gameProperties = gameProperties;
        random = new Random();

        size = Integer.parseInt(this.gameProperties.getProperty("size"));
        fertility = Integer.parseInt(gameProperties.getProperty("fertility"));
        radDisp = Integer.parseInt(gameProperties.getProperty("rad_disp"));

        cells = new HashMap<>();

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
                int x = random.nextInt(size);
                int y = random.nextInt(size);

                Position position = new Position(x, y);
                Cell cell = new Cell(position, group);
                cells.put(position, cell);
                updateView(cell);
            }
        }
    }

    public void tick() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            asexualReproductionTick();
        }));
        timeline.setCycleCount(100);
        timeline.play();
    }

    private void asexualReproductionTick() {
        HashMap<Position, Cell> allCells = new HashMap<>(cells);

        for (Cell cell : allCells.values()) {
            for (int i = 0; i < fertility; i++) {

                Cell newCell = createInRadius(cell, radDisp);
                Cell competitorCell = cells.get(newCell.getPosition());

                if (competitorCell != null) {
                    if (random.nextBoolean()) { // probability 50%
                        cells.put(newCell.getPosition(), newCell);
                        updateView(newCell);
                    }
                } else {
                    cells.put(newCell.getPosition(), newCell);
                    updateView(newCell);
                }
            }
        }
    }

    private void updateView(Cell cell) {
        Position position = cell.getPosition();
        Rectangle rectangle = (Rectangle) canvas.getChildren().get(position.getY() * size + position.getX());
        rectangle.setFill(COLORS[cell.getGroup()]);
    }

    private Cell createInRadius(Cell parent, int radius) {
        Position position = parent.getPosition();

        int minX = Math.max(0, position.getX() - radius);
        int maxX = Math.min(size - 1, position.getX() + radius);
        int minY = Math.max(0, position.getY() - radius);
        int maxY = Math.min(size - 1, position.getY() + radius);

        int x, y;

        do {
            x = minX + random.nextInt(maxX - minX + 1);
            y = minY + random.nextInt(maxY - minY + 1);
        } while (x == position.getX() && y == position.getY());

        return new Cell(new Position(x, y), parent.getGroup());
    }
}
