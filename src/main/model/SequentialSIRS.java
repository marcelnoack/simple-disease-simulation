package main.model;

import java.util.concurrent.ThreadLocalRandom;

public class SequentialSIRS implements IModelStrategy {
    @Override
    public void calcStep(int totalWidth, int totalHeight, CellStatus[][] field, int[][] rField) {
        int t;
        int x2 = 0;
        int y2 = 0;

        for (int x = 0; x < totalWidth; x++) {
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

    @Override
    public void calcRecovery(int totalWidth, int totalHeight, CellStatus[][] field, int[][] rField, int immunityDuration) {
        for (int x = 0; x < totalWidth; x++) {
            for (int y = 0; y < totalHeight; y++) {
                if (field[y][x] == CellStatus.INFECTED) {
                    rField[y][x] = rField[y][x] - 1;
                    if (rField[y][x] <= 0) {
                        field[y][x] = CellStatus.RECOVERED;
                        rField[y][x] = immunityDuration;
                    }
                } else if (field[y][x] == CellStatus.RECOVERED) {
                    rField[y][x] = rField[y][x] - 1;
                    if (rField[y][x] <= 0) {
                        field[y][x] = CellStatus.SUSCEPTIBLE;
                        rField[y][x] = 0;
                    }
                }
            }
        }
    }

    @Override
    public void calcInfection(int totalWidth, int totalHeight, double beta, CellStatus[][] field, int[][] rField, int immunityDuration) {
        for (int x = 0; x < totalWidth; x++) {
            for (int y = 0; y < totalHeight; y++) {
                if (field[y][x] == CellStatus.SUSCEPTIBLE) {

                    boolean getIll = false;
                    if (field[EpidemicState.yPos(y - 1, totalHeight)][EpidemicState.xPos(x - 1, totalWidth)] == CellStatus.INFECTED && Math.random() < beta) {
                        getIll = true;
                    }
                    if (field[EpidemicState.yPos(y - 1, totalHeight)][x] == CellStatus.INFECTED && Math.random() < beta) {
                        getIll = true;
                    }
                    if (field[EpidemicState.yPos(y - 1, totalHeight)][EpidemicState.xPos(x + 1, totalWidth)] == CellStatus.INFECTED && Math.random() < beta) {
                        getIll = true;
                    }
                    if (field[y][EpidemicState.xPos(x + 1, totalWidth)] == CellStatus.INFECTED && Math.random() < beta) {
                        getIll = true;
                    }
                    if (field[EpidemicState.yPos(y + 1, totalHeight)][EpidemicState.xPos(x + 1, totalWidth)] == CellStatus.INFECTED && Math.random() < beta) {
                        getIll = true;
                    }
                    if (field[EpidemicState.yPos(y + 1, totalHeight)][x] == CellStatus.INFECTED && Math.random() < beta) {
                        getIll = true;
                    }
                    if (field[EpidemicState.yPos(y + 1, totalHeight)][EpidemicState.xPos(x - 1, totalWidth)] == CellStatus.INFECTED && Math.random() < beta) {
                        getIll = true;
                    }
                    if (field[y][EpidemicState.xPos(x - 1, totalWidth)] == CellStatus.INFECTED && Math.random() < beta) {
                        getIll = true;
                    }

                    if (getIll == true) {
                        field[y][x] = CellStatus.INFECTED;
                        rField[y][x] = immunityDuration;
                    }
                }
            }
        }
    }
}
