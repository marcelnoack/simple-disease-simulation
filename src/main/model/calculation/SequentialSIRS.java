package main.model.calculation;

import main.model.EpidemicState;

import java.util.concurrent.ThreadLocalRandom;

public class SequentialSIRS implements IModelStrategy {
    @Override
    public void calcIteration(int totalWidth, int totalHeight, byte[][] field, short[][] rField, double beta, short immunityDuration, short infectionDuration) {
        calcStep(totalWidth, totalHeight, field, rField);
        calcRecovery(totalWidth, totalHeight, field, rField, immunityDuration);
        calcInfection(totalWidth, totalHeight, beta, field, rField, infectionDuration);
    }

    @Override
    public void calcStep(int totalWidth, int totalHeight, byte[][] field, short[][] rField) {
        int t;
        int x2 = 0;
        int y2 = 0;

        int x, y, xy;
        for (xy = 0; xy < totalWidth * totalHeight; xy++) {
            x = xy / totalWidth;
            y = xy % totalHeight;
            if (field[y][x] != 0) {
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

                if (field[y2][x2] == 0) EpidemicState.move(x, y, x2, y2, field, rField);
            }
        }
    }

    @Override
    public void calcRecovery(int totalWidth, int totalHeight, byte[][] field, short[][] rField, short immunityDuration) {
        int x, y, xy;
        for (xy = 0; xy < totalWidth * totalHeight; xy++) {
            x = xy / totalWidth;
            y = xy % totalHeight;
            if (field[y][x] == 2) {
                rField[y][x] = (short) (rField[y][x] - 1);
                if (rField[y][x] <= 0) {
                    field[y][x] = 3;
                    rField[y][x] = immunityDuration;
                }
            } else if (field[y][x] == 3) {
                rField[y][x] = (short) (rField[y][x] - 1);
                if (rField[y][x] <= 0) {
                    field[y][x] = 1;
                    rField[y][x] = 0;
                }
            }
        }
    }

    @Override
    public void calcInfection(int totalWidth, int totalHeight, double beta, byte[][] field, short[][] rField, short infectionDuration) {
        int x, y, xy;
        for (xy = 0; xy < totalWidth * totalHeight; xy++) {
            x = xy / totalWidth;
            y = xy % totalHeight;
            if (field[y][x] == 1) {

                boolean getIll = false;
                if (field[EpidemicState.yPos(y - 1, totalHeight)][EpidemicState.xPos(x - 1, totalWidth)] == 2 && Math.random() < beta) {
                    getIll = true;
                }
                if (field[EpidemicState.yPos(y - 1, totalHeight)][x] == 2 && Math.random() < beta) {
                    getIll = true;
                }
                if (field[EpidemicState.yPos(y - 1, totalHeight)][EpidemicState.xPos(x + 1, totalWidth)] == 2 && Math.random() < beta) {
                    getIll = true;
                }
                if (field[y][EpidemicState.xPos(x + 1, totalWidth)] == 2 && Math.random() < beta) {
                    getIll = true;
                }
                if (field[EpidemicState.yPos(y + 1, totalHeight)][EpidemicState.xPos(x + 1, totalWidth)] == 2 && Math.random() < beta) {
                    getIll = true;
                }
                if (field[EpidemicState.yPos(y + 1, totalHeight)][x] == 2 && Math.random() < beta) {
                    getIll = true;
                }
                if (field[EpidemicState.yPos(y + 1, totalHeight)][EpidemicState.xPos(x - 1, totalWidth)] == 2 && Math.random() < beta) {
                    getIll = true;
                }
                if (field[y][EpidemicState.xPos(x - 1, totalWidth)] == 2 && Math.random() < beta) {
                    getIll = true;
                }

                if (getIll == true) {
                    field[y][x] = 2;
                    rField[y][x] = infectionDuration;
                }
            }
        }
    }
}
