package game;

public final class Cell {
    private Position position;
    private int group;

    public Cell(Position position, int group) {
        this.position = position;
        this.group = group;
    }

    public int getGroup() {
        return group;
    }

    public Position getPosition() {
        return position;
    }
}
