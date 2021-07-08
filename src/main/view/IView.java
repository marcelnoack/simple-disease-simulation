package main.view;

import javafx.scene.Scene;
import main.model.CellStatus;

public interface IView {
    public Scene getScene();
    public void updateImage(CellStatus[][] newField);
    public void updateLabel(int step, int initialCellCount, int sCount, int iCount, int rCount);
    public void resetImage(int x0, int x1, int y0, int y1, CellStatus[][] field);
}
