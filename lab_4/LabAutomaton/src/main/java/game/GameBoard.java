package game;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

public class GameBoard {
    public static final int PADDING = 0;
    public static final int CELL_SIZE = 30;
    public static final double STROKE_WIDTH = 2.0;
    public static final Color STROKE_COLOR = Color.BLACK;
    public static final Color[] COLORS = {
            Color.rgb(52, 152, 219),
            Color.rgb(231, 76, 60),
            Color.rgb(241, 196, 15),
            Color.rgb(26, 188, 156),
            Color.rgb(155, 89, 182)
    };

    private Rectangle[][] rectangles;
    private ArrayList<Cell> cells;
    private Properties gameProperties;
    private Random random;

    private int size;

    public GameBoard(Group canvas, Properties gameProperties) {
        this.gameProperties = gameProperties;
        random = new Random();
        cells = new ArrayList<>();

        size = Integer.parseInt(this.gameProperties.getProperty("size"));
        rectangles = new Rectangle[size][size];

        drawGrid(canvas);

        settle();
    }

    private void drawGrid(Group canvas) {
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
                rectangles[i][j] = rect;
            }
        }
    }

    public void tick() {
        asexualReproduction();
    }

    private void asexualReproduction() {
        int fertility = Integer.parseInt(this.gameProperties.getProperty("fertility"));
        int radDisp = Integer.parseInt(this.gameProperties.getProperty("rad_disp"));
        ArrayList<Cell> newCells = new ArrayList<>();

        for (Cell cell : cells) {
            for (int i = 0; i < fertility; i++) {
                Cell newCell = createRandomCell(cell, radDisp);
                newCells.add(newCell);
                rectangles[newCell.i][newCell.j].setFill(COLORS[newCell.group]);
            }
        }
        cells.addAll(newCells);
    }

    private Cell createRandomCell(Cell parent, int radius) {
        int minI = Math.max(0, parent.i - radius), maxI = Math.min(size - 1, parent.i + radius),
                minJ = Math.max(0, parent.j - radius), maxJ = Math.min(size - 1, parent.j + radius);

        int i, j;

        do {
            i = minI + random.nextInt(maxI - minI + 1);
            j = minJ + random.nextInt(maxJ - minJ + 1);
        } while (i == parent.i && j == parent.j);

        return new Cell(i, j, parent.group);
    }

    public void settle() {
        double density = Double.parseDouble(gameProperties.getProperty("density"));
        int numberOfGroups = Integer.parseInt(gameProperties.getProperty("no_of_spp"));

        int groupSize = (int) (size * size * density / numberOfGroups);

        cells.ensureCapacity(groupSize * numberOfGroups);

        assert numberOfGroups < 6;

        for (int g = 0; g < numberOfGroups; g++) {
            for (int s = 0; s < groupSize; s++) {
                int i = random.nextInt(size);
                int j = random.nextInt(size);
                rectangles[i][j].setFill(COLORS[g]);
                cells.add(new Cell(i, j, g));
            }
        }
    }
}
