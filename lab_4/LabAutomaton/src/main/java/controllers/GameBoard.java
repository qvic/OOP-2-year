package controllers;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Properties;
import java.util.Random;

public class GameBoard {
    public static final int PADDING = 0;
    public static final int CELL_SIZE = 30;
    public static final double STROKE_WIDTH = 2.0;
    public static final Color STROKE_COLOR = Color.BLACK;

    private Rectangle[][] cells;
    private Properties gameProperties;
    private Random random;

    public GameBoard(Group canvas, Properties gameProperties) {
        this.gameProperties = gameProperties;
        this.random = new Random();

        int size = Integer.parseInt(this.gameProperties.getProperty("size"));
        cells = new Rectangle[size][size];
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
                cells[i][j] = rect;
            }
        }

        settle();
    }

    public void settle() {
        int size = Integer.parseInt(gameProperties.getProperty("size"));
        double density = Double.parseDouble(gameProperties.getProperty("density"));
        int numberOfGroups = Integer.parseInt(gameProperties.getProperty("no_of_spp"));

        int groupSize = (int) (size * size * density / numberOfGroups);

        Color[] colors = {
                Color.rgb(52, 152, 219),
                Color.rgb(231, 76, 60),
                Color.rgb(241, 196, 15),
                Color.rgb(26, 188, 156)
        };
        for (int g = 0; g < numberOfGroups; g++) {
            for (int s = 0; s < groupSize; s++) {
                int i = random.nextInt(size);
                int j = random.nextInt(size);
                cells[i][j].setFill(colors[g]);
            }
        }
    }
}
