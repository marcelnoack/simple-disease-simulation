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
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                this.field[x][y] = CellStatus.EMPTY;
                this.rField[x][y] = configuration.getImmunityDuration();
            }
        }

        randPerm(configuration.getS(), CellStatus.SUSCEPTIBLE);

        if (configuration.getInitialDistribution().size() > 0) {
            for (Cell c : configuration.getInitialDistribution()) {
                this.field[c.getX()][c.getY()] = c.getStatus();
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
            field[x][y] = cellStatus;
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

    private int xPos(int x) {
        int x2 = x;
        if (x2 == -1) x2 = configuration.getTotalWidth() - 1;
        if (x2 == configuration.getTotalWidth()) x2 = 0;
        return x2;
    }

    private int yPos(int y) {
        int y2 = y;
        if (y2 == -1) y2 = configuration.getTotalHeight() - 1;
        if (y2 == configuration.getTotalHeight()) y2 = 0;
        return y2;
    }

    private void move(int x1, int y1, int x2, int y2) {
        CellStatus cellStatus = field[x1][y1];
        field[x1][y1] = field[x2][y2];
        field[x2][y2] = cellStatus;

        int h = rField[x1][y1];
        rField[x1][y1] = rField[x2][y2];
        rField[x2][y2] = h;
    }

    private void calcStep() {
        int t;
        int x2 = 0;
        int y2 = 0;

        for (int x = 0; x < configuration.getTotalHeight(); x++) {
            for (int y = 0; y < configuration.getTotalWidth(); y++) {
                if (field[x][y] != CellStatus.EMPTY) {
                    t = ThreadLocalRandom.current().nextInt(8);

                    if (t == 0) {
                        x2 = xPos(x - 1);
                        y2 = yPos(y - 1);
                    } else if (t == 1) {
                        x2 = x;
                        y2 = yPos(y - 1);
                    } else if (t == 2) {
                        x2 = xPos(x + 1);
                        y2 = yPos(y - 1);
                    } else if (t == 3) {
                        x2 = xPos(x + 1);
                        y2 = y;
                    } else if (t == 4) {
                        x2 = xPos(x + 1);
                        y2 = yPos(y + 1);
                    } else if (t == 5) {
                        x2 = x;
                        y2 = yPos(y + 1);
                    } else if (t == 6) {
                        x2 = xPos(x - 1);
                        y2 = yPos(y + 1);
                    } else if (t == 7) {
                        x2 = xPos(x - 1);
                        y2 = y;
                    }

                    if (field[x2][y2] == CellStatus.EMPTY) {
                        // frei?
                        move(x, y, x2, y2);
                    }
                }
            }
        }
    }

    private void calcRecovery() {
        for (var x = 0; x < configuration.getTotalHeight(); x++) {
            for (var y = 0; y < configuration.getTotalWidth(); y++) {
                if (field[x][y] == CellStatus.INFECTED) {
                    rField[x][y] = rField[x][y] - 1;
                    if (rField[x][y] <= 0) {
                        field[x][y] = CellStatus.RECOVERED;
                        rField[x][y] = configuration.getInfectionDuration();
                    }
                } else if (field[x][y] == CellStatus.RECOVERED) {
                    rField[x][y] = rField[x][y] - 1;
                    if (rField[x][y] <= 0) {
                        field[x][y] = CellStatus.SUSCEPTIBLE;
                        rField[x][y] = 0;
                    }
                }
            }
        }
    }

    private void calcInfection() {
        for (int x = 0; x < configuration.getTotalHeight(); x++) {
            for (int y = 0; y < configuration.getTotalWidth(); y++) {
                if (field[x][y] == CellStatus.SUSCEPTIBLE) {

                    boolean getIll = false;
                    if (field[xPos(x - 1)][yPos(y - 1)] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
                        getIll = true;
                    }
                    if (field[x][yPos(y - 1)] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
                        getIll = true;
                    }
                    if (field[xPos(x + 1)][yPos(y - 1)] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
                        getIll = true;
                    }
                    if (field[xPos(x + 1)][y] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
                        getIll = true;
                    }
                    if (field[xPos(x + 1)][yPos(y + 1)] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
                        getIll = true;
                    }
                    if (field[x][yPos(y + 1)] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
                        getIll = true;
                    }
                    if (field[xPos(x - 1)][yPos(y + 1)] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
                        getIll = true;
                    }
                    if (field[xPos(x - 1)][y] == CellStatus.INFECTED && Math.random() < configuration.getBeta()) {
                        getIll = true;
                    }

                    if (getIll == true) {
                        field[x][y] = CellStatus.INFECTED;
                        rField[x][y] = configuration.getImmunityDuration();
                    }
                }
            }
        }
    }

    private void calcSIRCounts() {
        this.S = 0;
        this.I = 0;
        this.R = 0;
        for(int x = 0; x < configuration.getTotalWidth(); x++) {
            for(int y = 0; y < configuration.getTotalHeight(); y++) {
                if(field[x][y] == CellStatus.SUSCEPTIBLE) this.S++;
                if(field[x][y] == CellStatus.INFECTED) this.I++;
                if(field[x][y] == CellStatus.RECOVERED) this.R++;
            }
        }
    }

    private void incrementCurrentStep() {
        this.currentStep++;
    }

    public void handleIteration() {
        if (configuration.getMaxSteps() == 0 || (configuration.getMaxSteps() != 0 && currentStep <= configuration.getMaxSteps())) {
            calcStep();
            calcRecovery();
            calcInfection();
            incrementCurrentStep();
            calcSIRCounts();
        }
    }
}
