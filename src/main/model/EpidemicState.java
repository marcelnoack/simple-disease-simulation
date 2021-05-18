package main.model;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class EpidemicState {
    private int S, I, R;
    private int currentStep;
    private CellStatus[][] field;
    private int[][] rField;
    private Configuration configuration;

    public EpidemicState(Configuration configuration) {
        initState(configuration);
    }

    public void initState(Configuration configuration) {
        this.configuration = configuration;
        this.S = configuration.getS();
        this.I = configuration.getI();
        this.R = configuration.getR();
        this.currentStep = 0;
        initField();
    }

    private void initField() {
        int rows = configuration.getTotalHeight();
        int columns = configuration.getTotalWidth();

        this.field = new CellStatus[rows][columns];
        this.rField = new int[rows][columns];
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                this.field[y][x] = CellStatus.EMPTY;
                this.rField[y][x] = 0;
            }
        }

        randPerm(configuration.getS(), CellStatus.SUSCEPTIBLE);

        if (configuration.getInitialDistribution().size() > 0) {
            for (Cell c : configuration.getInitialDistribution()) {
                this.field[c.getY()][c.getX()] = c.getStatus();
                if(c.getStatus() == CellStatus.INFECTED) rField[c.getY()][c.getX()] = configuration.getInfectionDuration();
                if(c.getStatus() == CellStatus.RECOVERED) rField[c.getY()][c.getX()] = configuration.getImmunityDuration();
            }
        } else {
            randPerm(configuration.getI(), CellStatus.INFECTED);
            randPerm(configuration.getR(), CellStatus.RECOVERED);
        }
    }

    private void randPerm(int count, CellStatus cellStatus) {
        for (int c = 0; c < count; c++) {
            int x = ThreadLocalRandom.current().nextInt(configuration.getTotalWidth());
            int y = ThreadLocalRandom.current().nextInt(configuration.getTotalHeight());
            field[y][x] = cellStatus;
            if(cellStatus == CellStatus.INFECTED) rField[y][x] = configuration.getInfectionDuration();
            if(cellStatus == CellStatus.RECOVERED) rField[y][x] = configuration.getImmunityDuration();
        }
    }

    public int getCurrentSusceptibleCount() {
        return this.S;
    }

    public int getCurrentInfectedCount() {
        return this.I;
    }

    public int getCurrentRecoveredCount() {
        return this.R;
    }

    public int getCurrentStep() {
        return this.currentStep;
    }

    public CellStatus[][] getCurrentField() {
        return this.field;
    }

    public static int xPos(int x, int totalWidth) {
        int x2 = x;
        if (x2 == -1) x2 = totalWidth - 1;
        if (x2 == totalWidth) x2 = 0;
        return x2;
    }

    public static int yPos(int y, int totalHeight) {
        int y2 = y;
        if (y2 == -1) y2 = totalHeight - 1;
        if (y2 == totalHeight) y2 = 0;
        return y2;
    }

    public static void move(int x1, int y1, int x2, int y2, CellStatus[][] field, int[][] rField) {
        CellStatus cellStatus = field[y1][x1];
        field[y1][x1] = field[y2][x2];
        field[y2][x2] = cellStatus;

        int h = rField[y1][x1];
        rField[y1][x1] = rField[y2][x2];
        rField[y2][x2] = h;
    }

//    private void calcStep() {
//        int t;
//        int x2 = 0;
//        int y2 = 0;
//
//        for (int x = 0; x < configuration.getTotalWidth(); x++) {
//            for (int y = 0; y < configuration.getTotalHeight(); y++) {
//                if (field[y][x] != CellStatus.EMPTY) {
//                    t = ThreadLocalRandom.current().nextInt(8);
//
//                    if (t == 0) {
//                        x2 = xPos(x - 1);
//                        y2 = yPos(y - 1);
//                    } else if (t == 1) {
//                        x2 = x;
//                        y2 = yPos(y - 1);
//                    } else if (t == 2) {
//                        x2 = xPos(x + 1);
//                        y2 = yPos(y - 1);
//                    } else if (t == 3) {
//                        x2 = xPos(x + 1);
//                        y2 = y;
//                    } else if (t == 4) {
//                        x2 = xPos(x + 1);
//                        y2 = yPos(y + 1);
//                    } else if (t == 5) {
//                        x2 = x;
//                        y2 = yPos(y + 1);
//                    } else if (t == 6) {
//                        x2 = xPos(x - 1);
//                        y2 = yPos(y + 1);
//                    } else if (t == 7) {
//                        x2 = xPos(x - 1);
//                        y2 = y;
//                    }
//
//                    if (field[y2][x2] == CellStatus.EMPTY) {
//                        // frei?
//                        move(x, y, x2, y2);
//                    }
//                }
//            }
//        }
//    }
//
//    private void calcRecovery() {
//        for (int x = 0; x < configuration.getTotalWidth(); x++) {
//            for (int y = 0; y < configuration.getTotalHeight(); y++) {
//                if (field[y][x] == CellStatus.INFECTED) {
//                    rField[y][x] = rField[y][x] - 1;
//                    if (rField[y][x] <= 0) {
//                        field[y][x] = CellStatus.RECOVERED;
//                        rField[y][x] = configuration.getImmunityDuration();
//                    }
//                } else if (field[y][x] == CellStatus.RECOVERED) {
//                    rField[y][x] = rField[y][x] - 1;
//                    if (rField[y][x] <= 0) {
//                        field[y][x] = CellStatus.SUSCEPTIBLE;
//                        rField[y][x] = 0;
//                    }
//                }
//            }
//        }
//    }
//
//    private void calcInfection() {
//        for (int x = 0; x < configuration.getTotalWidth(); x++) {
//            for (int y = 0; y < configuration.getTotalHeight(); y++) {
//                if (field[y][x] == CellStatus.SUSCEPTIBLE) {
//
//                    boolean getIll = false;
//                    if (field[yPos(y - 1)][xPos(x - 1)] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
//                        getIll = true;
//                    }
//                    if (field[yPos(y - 1)][x] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
//                        getIll = true;
//                    }
//                    if (field[yPos(y - 1)][xPos(x + 1)] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
//                        getIll = true;
//                    }
//                    if (field[y][xPos(x + 1)] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
//                        getIll = true;
//                    }
//                    if (field[yPos(y + 1)][xPos(x + 1)] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
//                        getIll = true;
//                    }
//                    if (field[yPos(y + 1)][x] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
//                        getIll = true;
//                    }
//                    if (field[yPos(y + 1)][xPos(x - 1)] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
//                        getIll = true;
//                    }
//                    if (field[y][xPos(x - 1)] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
//                        getIll = true;
//                    }
//
//                    if (getIll == true) {
//                        field[y][x] = CellStatus.INFECTED;
//                        rField[y][x] = configuration.getImmunityDuration();
//                    }
//                }
//            }
//        }
//    }

    private void calcSIRCounts() {
        this.S = 0;
        this.I = 0;
        this.R = 0;
        for(int x = 0; x < configuration.getTotalWidth(); x++) {
            for(int y = 0; y < configuration.getTotalHeight(); y++) {
                if(field[y][x] == CellStatus.SUSCEPTIBLE) this.S++;
                if(field[y][x] == CellStatus.INFECTED) this.I++;
                if(field[y][x] == CellStatus.RECOVERED) this.R++;
            }
        }
    }

    private void incrementCurrentStep() {
        this.currentStep++;
    }

    public void handleIteration() {
        if (configuration.getMaxSteps() == 0 || (configuration.getMaxSteps() != 0 && currentStep <= configuration.getMaxSteps())) {
            IModelStrategy modelStrategy = ModelFactory.getModelStrategy(configuration.getMode(), Configuration.MODEL);
            modelStrategy.calcStep(configuration, field, rField);
            modelStrategy.calcRecovery(configuration.getTotalWidth(), configuration.getTotalHeight(), field, rField, configuration.getImmunityDuration());
            modelStrategy.calcInfection(configuration.getTotalWidth(), configuration.getTotalHeight(), configuration.getBeta(), field, rField, configuration.getImmunityDuration());
            incrementCurrentStep();
            calcSIRCounts();
        }
    }
}
