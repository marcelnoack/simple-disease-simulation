package main.model.types;

public class Cell {
    private int x, y;
    private CellStatus status;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public CellStatus getStatus() {
        return status;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setStatus(CellStatus status) {
        this.status = status;
    }
}
