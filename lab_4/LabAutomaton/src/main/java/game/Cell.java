package game;

import java.util.concurrent.atomic.AtomicBoolean;

public class Cell {
    private Position position;
    private int group;
    private AtomicBoolean locked = new AtomicBoolean(false);

    public boolean lock() {
        // if the cell is locked, no one can interbreed with it
        // returns was the lock successful
        return locked.compareAndSet(false, true);
    }

    public boolean isLocked() {
        return locked.get();
    }

    public Cell(Position position, int group) {
        this.position = position;
        this.group = group;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "position=" + position +
                ", group=" + group +
                '}';
    }

    public int getGroup() {
        return group;
    }

    public Position getPosition() {
        return position;
    }

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }
}
