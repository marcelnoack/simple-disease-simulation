package main.model;

import main.model.calculation.IModelStrategy;
import main.model.calculation.ModelFactory;
import main.model.types.Cell;
import main.model.types.CellStatus;

import java.util.concurrent.ThreadLocalRandom;

public class EpidemicState {
    private int S, I, R;
    private int currentStep;
    // byte data type to reduce the required memory allocation as much as possible
    // EMPTY = 0; SUSCEPTIBLE = 1; INFECTED = 2; RECOVERED = 3;
    private byte[][] field;
    // short data type to be able to support immunity and infection durations above 128
    // + while still being able to reduce the required memory allocation
    private short[][] rField;
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

        this.field = new byte[rows][columns];
        this.rField = new short[rows][columns];
        int x, y, xy;
        for (xy = 0; xy < columns * rows; xy++) {
            x = xy / columns;
            y = xy % rows;
            this.field[y][x] = 0;
            this.rField[y][x] = 0;
        }

        randPerm(configuration.getS(), (byte) 1);

        if (configuration.getInitialDistribution().size() > 0) {
            for (Cell c : configuration.getInitialDistribution()) {
                byte statusValue = 0;
                if (c.getStatus() == CellStatus.SUSCEPTIBLE) statusValue = 1;
                if (c.getStatus() == CellStatus.INFECTED) statusValue = 2;
                if (c.getStatus() == CellStatus.RECOVERED) statusValue = 3;
                this.field[c.getY()][c.getX()] = statusValue;
                if (c.getStatus() == CellStatus.INFECTED)
                    rField[c.getY()][c.getX()] = configuration.getInfectionDuration();
                if (c.getStatus() == CellStatus.RECOVERED)
                    rField[c.getY()][c.getX()] = configuration.getImmunityDuration();
            }
        } else {
            randPerm(configuration.getI(), (byte) 2);
            randPerm(configuration.getR(), (byte) 3);
        }
    }

    private void randPerm(int count, byte cellStatus) {
        for (int c = 0; c < count; c++) {
            int x = ThreadLocalRandom.current().nextInt(configuration.getTotalWidth());
            int y = ThreadLocalRandom.current().nextInt(configuration.getTotalHeight());
            field[y][x] = cellStatus;
            if (cellStatus == 2) rField[y][x] = configuration.getInfectionDuration();
            if (cellStatus == 3) rField[y][x] = configuration.getImmunityDuration();
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

    public byte[][] getCurrentField() {
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

    public static void move(int x1, int y1, int x2, int y2, byte[][] field, short[][] rField) {
        byte cellStatus = field[y1][x1];
        field[y1][x1] = field[y2][x2];
        field[y2][x2] = cellStatus;

        int h = rField[y1][x1];
        rField[y1][x1] = rField[y2][x2];
        rField[y2][x2] = (short) h;
    }

    private void calcSIRCounts() {
        this.S = 0;
        this.I = 0;
        this.R = 0;
        int x, y, xy;

        for (xy = 0; xy < configuration.getTotalWidth() * configuration.getTotalHeight(); xy++) {
            x = xy / configuration.getTotalWidth();
            y = xy % configuration.getTotalHeight();
            if (field[y][x] == 1) this.S++;
            if (field[y][x] == 2) this.I++;
            if (field[y][x] == 3) this.R++;
        }
    }

    private void incrementCurrentStep() {
        this.currentStep++;
    }

    public void handleIteration() {
        if (configuration.getMaxSteps() == 0 || (configuration.getMaxSteps() != 0 && currentStep <= configuration.getMaxSteps())) {
            IModelStrategy modelStrategy = ModelFactory.getModelStrategy(configuration.getMode(), Configuration.MODEL);
            modelStrategy.calcIteration(configuration.getTotalWidth(), configuration.getTotalHeight(), field, rField, configuration.getBeta(), configuration.getImmunityDuration(), configuration.getInfectionDuration());
            calcSIRCounts();
            incrementCurrentStep();
        }
    }
}
