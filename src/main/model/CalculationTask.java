package main.model;

import java.util.concurrent.ThreadLocalRandom;

public class CalculationTask implements Runnable{
    int totalWidth, totalHeight, from, to;
    CellStatus[][] field;
    int[][] rField;

    CalculationTask(int totalWidth, int totalHeight, CellStatus[][] field, int[][] rField, int from, int to) {
        this.totalWidth = totalWidth;
        this.totalHeight = totalHeight;
        this.field = field;
        this.rField = rField;
        this.from = from;
        this.to = to;
    }
    @Override
    public void run() {
        int t;
        int x2 = 0;
        int y2 = 0;
        for (int x = this.from; x < this.to; x++) {
            for (int y = 0; y < totalHeight; y++) {
                if (field[y][x] != CellStatus.EMPTY) {
                    t = ThreadLocalRandom.current().nextInt(8);

                    if (t == 0) {
                        x2 = EpidemicState.xPos(x - 1, totalWidth);
                        y2 = EpidemicState.yPos(y - 1, totalHeight);
                    } else if (t == 1) {
                        x2 = x;
                        y2 = EpidemicState.yPos(y - 1, totalHeight);
                    } else if (t == 2) {
                        x2 = EpidemicState.xPos(x + 1, totalWidth);
                        y2 = EpidemicState.yPos(y - 1, totalHeight);
                    } else if (t == 3) {
                        x2 = EpidemicState.xPos(x + 1, totalWidth);
                        y2 = y;
                    } else if (t == 4) {
                        x2 = EpidemicState.xPos(x + 1, totalWidth);
                        y2 = EpidemicState.yPos(y + 1, totalHeight);
                    } else if (t == 5) {
                        x2 = x;
                        y2 = EpidemicState.yPos(y + 1, totalHeight);
                    } else if (t == 6) {
                        x2 = EpidemicState.xPos(x - 1, totalWidth);
                        y2 = EpidemicState.yPos(y + 1, totalHeight);
                    } else if (t == 7) {
                        x2 = EpidemicState.xPos(x - 1, totalWidth);
                        y2 = y;
                    }

                    if (field[y2][x2] == CellStatus.EMPTY) EpidemicState.move(x, y, x2, y2, field, rField);
                }
            }
        }
    }
}
