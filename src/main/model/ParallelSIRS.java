package main.model;

import java.util.concurrent.ThreadLocalRandom;

public class ParallelSIRS implements IModelStrategy {
    private static final int THREAD_COUNT = 8;

    @Override
    public void calcIteration(int totalWidth, int totalHeight, CellStatus[][] field, int[][] rField, double beta, int immunityDuration, int infectionDuration) {
        calcStep(totalWidth, totalHeight, field, rField);
        calcRecovery(totalWidth, totalHeight, field, rField, immunityDuration);
        calcInfection(totalWidth, totalHeight, beta, field, rField, infectionDuration);
    }

    @Override
    public void calcStep(int totalWidth, int totalHeight, CellStatus[][] field, int[][] rField) {
        Thread[] threads = new Thread[THREAD_COUNT];
        int divisibleWidth = _calcDivisibleWidth(totalWidth - 1, THREAD_COUNT - 1);
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    int t = 0;
                    int x2 = 0;
                    int y2 = 0;

                    if (totalWidth % THREAD_COUNT == 0) {
                        for (int x = (totalWidth / THREAD_COUNT) * threadIndex; x < (totalWidth / THREAD_COUNT) * (threadIndex + 1); x++) {
                            for (int y = 0; y < totalHeight; y++) {
                                _calcStepCell(field, rField, x, y, t, x2, y2, totalWidth, totalHeight);
                            }
                        }
                    } else {
                        if (threadIndex == THREAD_COUNT - 1) { // allocate non-divisible rest to the last thread
                            for (int x = divisibleWidth; x < totalWidth; x++) {
                                for (int y = 0; y < totalHeight; y++) {
                                    _calcStepCell(field, rField, x, y, t, x2, y2, totalWidth, totalHeight);
                                }
                            }
                        } else {
                            for (int x = ((divisibleWidth / (THREAD_COUNT - 1)) * threadIndex); x < ((divisibleWidth / (THREAD_COUNT - 1)) * (threadIndex + 1)); x++) {
                                for (int y = 0; y < totalHeight; y++) {
                                    _calcStepCell(field, rField, x, y, t, x2, y2, totalWidth, totalHeight);
                                }
                            }
                        }
                    }
                }
            });
            threads[i].setName("StepThread" + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void _calcStepCell(CellStatus[][] field, int[][] rField, int x, int y, int t, int x2, int y2, int totalWidth, int totalHeight) {
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

    @Override
    public void calcRecovery(int totalWidth, int totalHeight, CellStatus[][] field, int[][] rField, int immunityDuration) {
        Thread[] threads = new Thread[THREAD_COUNT];
        int divisibleWidth = _calcDivisibleWidth(totalWidth - 1, THREAD_COUNT - 1);
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (totalWidth % THREAD_COUNT == 0) {
                        for (int x = ((totalWidth / THREAD_COUNT) * threadIndex); x < ((totalWidth / THREAD_COUNT) * (threadIndex + 1)); x++) {
                            for (int y = 0; y < totalHeight; y++) {
                                _calcRecoveryCell(field, rField, x, y, immunityDuration);
                            }
                        }
                    } else {
                        if (threadIndex == THREAD_COUNT - 1) { // allocate non-divisible rest to the last thread
                            for (int x = divisibleWidth; x < totalWidth; x++) {
                                for (int y = 0; y < totalHeight; y++) {
                                    _calcRecoveryCell(field, rField, x, y, immunityDuration);
                                }
                            }
                        } else {
                            for (int x = ((divisibleWidth / (THREAD_COUNT - 1)) * threadIndex); x < ((divisibleWidth / (THREAD_COUNT - 1)) * (threadIndex + 1)); x++) {
                                for (int y = 0; y < totalHeight; y++) {
                                    _calcRecoveryCell(field, rField, x, y, immunityDuration);
                                }
                            }
                        }
                    }
                }
            });
            threads[i].setName("recoveryThread" + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void _calcRecoveryCell(CellStatus[][] field, int[][] rField, int x, int y, int immunityDuration) {
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

    @Override
    public void calcInfection(int totalWidth, int totalHeight, double beta, CellStatus[][] field, int[][] rField, int infectionDuration) {
        Thread[] threads = new Thread[THREAD_COUNT];
        int divisibleWidth = _calcDivisibleWidth(totalWidth - 1, THREAD_COUNT - 1);
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (totalWidth % THREAD_COUNT == 0) {
                        for (int x = ((totalWidth / THREAD_COUNT) * threadIndex); x < ((totalWidth / THREAD_COUNT) * (threadIndex + 1)); x++) {
                            for (int y = 0; y < totalHeight; y++) {
                                _calcInfectionCell(field, rField, x, y, totalWidth, totalHeight, beta, infectionDuration);
                            }
                        }
                    } else {
                        if (threadIndex == THREAD_COUNT - 1) { // allocate non-divisible rest to the last thread
                            for (int x = divisibleWidth; x < totalWidth; x++) {
                                for (int y = 0; y < totalHeight; y++) {
                                    _calcInfectionCell(field, rField, x, y, totalWidth, totalHeight, beta, infectionDuration);
                                }
                            }
                        } else {
                            for (int x = ((divisibleWidth / (THREAD_COUNT - 1)) * threadIndex); x < ((divisibleWidth / (THREAD_COUNT - 1)) * (threadIndex + 1)); x++) {
                                for (int y = 0; y < totalHeight; y++) {
                                    _calcInfectionCell(field, rField, x, y, totalWidth, totalHeight, beta, infectionDuration);;
                                }
                            }
                        }
                    }
                }
            });
            threads[i].setName("infectionThread" + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void _calcInfectionCell(CellStatus[][] field, int[][] rField, int x, int y, int totalWidth, int totalHeight, double beta, int infectionDuration) {
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
                rField[y][x] = infectionDuration;
            }
        }
    }

    private int _calcDivisibleWidth(int width, int threadCount) {
        if (width % threadCount == 0) return width;
        return _calcDivisibleWidth(width - 1, threadCount);
    }
}

////////////////////////////////////////////////////
//        int nrOfProcessors = Runtime.getRuntime().availableProcessors();
//        ExecutorService executor = Executors.newFixedThreadPool(nrOfProcessors);
//        for(int i = 0; i < nrOfProcessors; i++) {
//            executor.execute(new CalculationTask(totalWidth, totalHeight, field, rField, (totalWidth / nrOfProcessors) * i, (totalWidth / nrOfProcessors) * (i + 1)));
//        }
//        executor.shutdown();
///////////////////////////////////////////////////
//                    int startColumn = (totalWidth / THREAD_COUNT) * temp;
//                    int startRow = 0;
//                    int columns = (totalWidth / THREAD_COUNT) * (temp + 1);
//                    int rows = totalHeight;
//                    int x, y, xy;
//                    for(xy = startColumn; xy < columns * rows; xy++) {
//                        x = xy / columns;
//                        y = xy % rows;
//                        if (field[y][x] != CellStatus.EMPTY) {
//                            t = ThreadLocalRandom.current().nextInt(8);
//
//                            if (t == 0) {
//                                x2 = EpidemicState.xPos(x - 1, totalWidth);
//                                y2 = EpidemicState.yPos(y - 1, totalHeight);
//                            } else if (t == 1) {
//                                x2 = x;
//                                y2 = EpidemicState.yPos(y - 1, totalHeight);
//                            } else if (t == 2) {
//                                x2 = EpidemicState.xPos(x + 1, totalWidth);
//                                y2 = EpidemicState.yPos(y - 1, totalHeight);
//                            } else if (t == 3) {
//                                x2 = EpidemicState.xPos(x + 1, totalWidth);
//                                y2 = y;
//                            } else if (t == 4) {
//                                x2 = EpidemicState.xPos(x + 1, totalWidth);
//                                y2 = EpidemicState.yPos(y + 1, totalHeight);
//                            } else if (t == 5) {
//                                x2 = x;
//                                y2 = EpidemicState.yPos(y + 1, totalHeight);
//                            } else if (t == 6) {
//                                x2 = EpidemicState.xPos(x - 1, totalWidth);
//                                y2 = EpidemicState.yPos(y + 1, totalHeight);
//                            } else if (t == 7) {
//                                x2 = EpidemicState.xPos(x - 1, totalWidth);
//                                y2 = y;
//                            }
//
//                            if (field[y2][x2] == CellStatus.EMPTY) EpidemicState.move(x, y, x2, y2, field, rField);
//                        }
//                    }
