package main.model;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ParallelSIRS implements IModelStrategy {
    private static final int THREAD_COUNT = 8;
    private Lock lock = new ReentrantLock();

    @Override
    public void calcIteration(int totalWidth, int totalHeight, byte[][] field, short[][] rField, double beta, short immunityDuration, short infectionDuration) {
        calcStep(totalWidth, totalHeight, field, rField);
        calcRecovery(totalWidth, totalHeight, field, rField, immunityDuration);
        calcInfection(totalWidth, totalHeight, beta, field, rField, infectionDuration);
    }

    @Override
    public void calcStep(int totalWidth, int totalHeight, byte[][] field, short[][] rField) {
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
                                    if (x > (divisibleWidth + 1) || x < (totalWidth - 1)) {
                                        _calcStepCell(field, rField, x, y, t, x2, y2, totalWidth, totalHeight);
                                    } else {
                                        lock.lock();
                                        try {
                                            _calcStepCell(field, rField, x, y, t, x2, y2, totalWidth, totalHeight);
                                        } finally {
                                            lock.unlock();
                                        }
                                    }
                                }
                            }
                        } else {
                            int start = ((divisibleWidth / (THREAD_COUNT - 1)) * threadIndex);
                            int end = ((divisibleWidth / (THREAD_COUNT - 1)) * (threadIndex + 1));
                            for (int x = start; x < end; x++) {
                                for (int y = 0; y < totalHeight; y++) {
                                    if (x > (start + 1) || x < (end - 1)) {
                                        _calcStepCell(field, rField, x, y, t, x2, y2, totalWidth, totalHeight);
                                    } else {
                                        lock.unlock();
                                        try {
                                            _calcStepCell(field, rField, x, y, t, x2, y2, totalWidth, totalHeight);
                                        } finally {
                                            lock.unlock();
                                        }
                                    }
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

    private void _calcStepCell(byte[][] field, short[][] rField, int x, int y, int t, int x2, int y2, int totalWidth, int totalHeight) {
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

    @Override
    public void calcRecovery(int totalWidth, int totalHeight, byte[][] field, short[][] rField, short immunityDuration) {
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
                                    if (x > (divisibleWidth + 1) || x < (totalWidth - 1)) {
                                        _calcRecoveryCell(field, rField, x, y, immunityDuration);
                                    } else {
                                        lock.lock();
                                        try {
                                            _calcRecoveryCell(field, rField, x, y, immunityDuration);
                                        } finally {
                                            lock.unlock();
                                        }
                                    }
                                }
                            }
                        } else {
                            int start = ((divisibleWidth / (THREAD_COUNT - 1)) * threadIndex);
                            int end = ((divisibleWidth / (THREAD_COUNT - 1)) * (threadIndex + 1));
                            for (int x = start; x < end; x++) {
                                for (int y = 0; y < totalHeight; y++) {
                                    if (x > (start + 1) || x < (end - 1)) {
                                        _calcRecoveryCell(field, rField, x, y, immunityDuration);
                                    } else {
                                        lock.lock();
                                        try {
                                            _calcRecoveryCell(field, rField, x, y, immunityDuration);
                                        } finally {
                                            lock.unlock();
                                        }
                                    }
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

    private void _calcRecoveryCell(byte[][] field, short[][] rField, int x, int y, short immunityDuration) {
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

    @Override
    public void calcInfection(int totalWidth, int totalHeight, double beta, byte[][] field, short[][] rField, short infectionDuration) {
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
                                    if (x > (divisibleWidth + 1) || x < (totalWidth - 1)) {
                                        _calcInfectionCell(field, rField, x, y, totalWidth, totalHeight, beta, infectionDuration);
                                    } else {
                                        lock.lock();
                                        try {
                                            _calcInfectionCell(field, rField, x, y, totalWidth, totalHeight, beta, infectionDuration);
                                        } finally {
                                            lock.unlock();
                                        }
                                    }
                                }
                            }
                        } else {
                            int start = ((divisibleWidth / (THREAD_COUNT - 1)) * threadIndex);
                            int end = ((divisibleWidth / (THREAD_COUNT - 1)) * (threadIndex + 1));
                            for (int x = start; x < end; x++) {
                                for (int y = 0; y < totalHeight; y++) {
                                    if (x > (start + 1) || x < (end - 1)) {
                                        _calcInfectionCell(field, rField, x, y, totalWidth, totalHeight, beta, infectionDuration);
                                    } else {
                                        lock.lock();
                                        try {
                                            _calcInfectionCell(field, rField, x, y, totalWidth, totalHeight, beta, infectionDuration);
                                        } finally {
                                            lock.unlock();
                                        }
                                    }
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

    private void _calcInfectionCell(byte[][] field, short[][] rField, int x, int y, int totalWidth, int totalHeight, double beta, short infectionDuration) {
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
