package game;

public final class Cell {
    private Position position;
    private int group;

    public Cell(Position position, int group) {
        this.position = position;
        this.group = group;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "group=" + group +
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
