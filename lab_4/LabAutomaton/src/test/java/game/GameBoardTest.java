package game;

import javafx.scene.Group;
import org.junit.jupiter.api.RepeatedTest;
import test.util.JavaFxTest;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameBoardTest extends JavaFxTest {

    @RepeatedTest(100)
    void asexualReproductionTick() {

        Properties testProperties = new Properties();
        testProperties.setProperty("size", "5");
        testProperties.setProperty("rad_disp", "2");
        testProperties.setProperty("rad_breed", "2");
        testProperties.setProperty("sex_breed", "asexual");
        testProperties.setProperty("fertility", "4");
        testProperties.setProperty("density", "0.0");
        testProperties.setProperty("no_of_spp", "2");
        testProperties.setProperty("time", "1");

        int size = Integer.parseInt(testProperties.getProperty("size"));
        int radius = Integer.parseInt(testProperties.getProperty("rad_disp"));
        int fertility = Integer.parseInt(testProperties.getProperty("fertility"));

        Group canvas = new Group();

        GameBoard gameBoard = new GameBoard(canvas, testProperties);

        Cell cell1 = new Cell(new Position(2, 2), 0);
        gameBoard.put(cell1);

        Cell cell2 = new Cell(new Position(2, 3), 1);
        gameBoard.put(cell2);

        gameBoard.asexualReproductionTick();

        // guarantee all threads finished work
        waitUntilNoError(5000L, () -> {
            int cellsInRadius1 = numberOfCellsInRadius(cell1, radius, size, gameBoard);
            assertTrue(cellsInRadius1 <= fertility, "too much cells of group 0 - " + cellsInRadius1);

            int cellsInRadius2 = numberOfCellsInRadius(cell2, radius, size, gameBoard);
            assertTrue(cellsInRadius2 <= fertility, "too much cells of group 1 - " + cellsInRadius2);
        });
    }

    @RepeatedTest(100)
    void sexualReproductionTickNoCompetition() {

        Properties testProperties = new Properties();
        testProperties.setProperty("size", "5");
        testProperties.setProperty("rad_disp", "2");
        testProperties.setProperty("rad_breed", "1");
        testProperties.setProperty("sex_breed", "sexual");
        testProperties.setProperty("fertility", "3");
        testProperties.setProperty("density", "0.0");
        testProperties.setProperty("no_of_spp", "2");
        testProperties.setProperty("time", "1");

        int size = Integer.parseInt(testProperties.getProperty("size"));
        int radius = Integer.parseInt(testProperties.getProperty("rad_disp"));
        int fertility = Integer.parseInt(testProperties.getProperty("fertility"));

        Group canvas = new Group();

        GameBoard gameBoard = new GameBoard(canvas, testProperties);

        Cell cell1 = new Cell(new Position(2, 2), 0);
        gameBoard.put(cell1);

        Cell cell2 = new Cell(new Position(2, 3), 0);
        gameBoard.put(cell2);

        gameBoard.sexualReproductionTick();

        // guarantee all threads finished work
        waitUntilNoError(5000L, () -> {
            int cellsInRadius1 = numberOfCellsInRadius(cell1, radius, size, gameBoard);
            assertTrue(cellsInRadius1 == fertility, "too much cells of group 0 - " + cellsInRadius1);
            assertTrue(numberOfCellsInRadius(cell1, 0, size, gameBoard) == 0, "parent was not deleted");
        });
    }

    @RepeatedTest(100)
    void sexualReproductionTick() {

        Properties testProperties = new Properties();
        testProperties.setProperty("size", "5");
        testProperties.setProperty("rad_disp", "2");
        testProperties.setProperty("rad_breed", "1");
        testProperties.setProperty("sex_breed", "sexual");
        testProperties.setProperty("fertility", "3");
        testProperties.setProperty("density", "0.0");
        testProperties.setProperty("no_of_spp", "2");
        testProperties.setProperty("time", "1");

        int size = Integer.parseInt(testProperties.getProperty("size"));
        int radius = Integer.parseInt(testProperties.getProperty("rad_disp"));
        int fertility = Integer.parseInt(testProperties.getProperty("fertility"));

        Group canvas = new Group();

        GameBoard gameBoard = new GameBoard(canvas, testProperties);

        Cell cell1 = new Cell(new Position(2, 2), 0);
        gameBoard.put(cell1);

        Cell cell2 = new Cell(new Position(2, 3), 0);
        gameBoard.put(cell2);

        Cell cell3 = new Cell(new Position(3, 2), 1);
        gameBoard.put(cell3);

        Cell cell4 = new Cell(new Position(3, 3), 1);
        gameBoard.put(cell4);

        gameBoard.sexualReproductionTick();

        // guarantee all threads finished work
        waitUntilNoError(5000L, () -> {
            int cellsInRadius1 = numberOfCellsInRadius(cell1, radius, size, gameBoard);
            assertTrue(cellsInRadius1 <= fertility, "too much cells of group 0 - " + cellsInRadius1);

            int cellsInRadius3 = numberOfCellsInRadius(cell3, radius, size, gameBoard);
            assertTrue(cellsInRadius3 <= fertility, "too much cells of group 1 - " + cellsInRadius3);
        });
    }

    @RepeatedTest(100)
    void populateRadius() {

        Properties testProperties = new Properties();
        testProperties.setProperty("size", "5");
        testProperties.setProperty("rad_disp", "2");
        testProperties.setProperty("rad_breed", "2");
        testProperties.setProperty("sex_breed", "asexual");
        testProperties.setProperty("fertility", "10");
        testProperties.setProperty("density", "0.0");
        testProperties.setProperty("no_of_spp", "2");
        testProperties.setProperty("time", "1");

        int size = Integer.parseInt(testProperties.getProperty("size"));
        int radius = Integer.parseInt(testProperties.getProperty("rad_disp"));
        int fertility = Integer.parseInt(testProperties.getProperty("fertility"));

        Group canvas = new Group();

        GameBoard gameBoard = new GameBoard(canvas, testProperties);

        Cell parent = new Cell(new Position(2, 2), 0);
        gameBoard.put(parent);

        gameBoard.populateRadius(parent);

        assertEquals(fertility + 1, numberOfCellsInRadius(parent, radius, size, gameBoard));
    }

    private int numberOfCellsInRadius(Cell parent, int radius, int size, GameBoard gameBoard) {
        int numberOfCells = 0;
        Position position = parent.getPosition();

        int minX = Math.max(0, position.getX() - radius);
        int maxX = Math.min(size - 1, position.getX() + radius);
        int minY = Math.max(0, position.getY() - radius);
        int maxY = Math.min(size - 1, position.getY() + radius);

        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                Cell cell = gameBoard.get(i, j);
                if (cell != null && parent.getGroup() == cell.getGroup()) numberOfCells++;
            }
        }
        return numberOfCells;
    }

    private static  void waitUntilNoError(long millis, Runnable runnable) {
        long end = System.currentTimeMillis() + millis;

        while (System.currentTimeMillis() < end) {
            try {
                runnable.run();
                return;
            } catch (Throwable e1) {
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e2) {
                    throw new RuntimeException("Interrupted exception", e1);
                }
            }
        }
    }
}