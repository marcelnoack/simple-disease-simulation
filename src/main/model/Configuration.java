package main.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Configuration {
    // default values
    private static final int TOTAL_WIDTH = 1000, TOTAL_HEIGHT = 1000;
    private static final int X_0 = 300, X_1 = 600, Y_0 = 300, Y_1 = 600;
    private static final int S = 800000, I = 1, R = 0;
    private static final int MAX_STEPS = 0;
    private static final double BETA = 0.33;
    private static final CalculationMode MODE = CalculationMode.PARALLEL;
    public static final EpidemicModel MODEL = EpidemicModel.SIRS;

    private int totalWidth, totalHeight;
    private int x0, x1, y0, y1;
    private int s, i, r;
    private double beta;
    private int immunityDuration, infectionDuration;
    private int maxSteps;
    private ArrayList<Cell> initialDistribution;
    private CalculationMode mode;

    private enum DurationsMode {
        FIXED,
        RANDOM
    }

    public Configuration(int totalWidth, int totalHeight, int x0, int x1, int y0, int y1, int S, int I, int R, double beta, int immunityDuration, int infectionDuration, int maxSteps, ArrayList<Cell> initialDistribution, CalculationMode mode) {
        this.totalWidth = totalWidth;
        this.totalHeight = totalHeight;
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
        this.s = S;
        this.i = I;
        this.r = R;
        this.beta = beta;
        this.immunityDuration = immunityDuration;
        this.infectionDuration = infectionDuration;
        this.maxSteps = maxSteps;
        this.initialDistribution = initialDistribution;
        this.mode = mode;

        System.out.println(this.toString());
    }

    public int getTotalWidth() {
        return totalWidth;
    }

    public int getTotalHeight() {
        return totalHeight;
    }

    public int getX0() {
        return x0;
    }

    public int getX1() {
        return x1;
    }

    public int getY0() {
        return y0;
    }

    public int getY1() {
        return y1;
    }

    public int getS() {
        return s;
    }

    public int getI() {
        return i;
    }

    public int getR() {
        return r;
    }

    public double getBeta() {
        return beta;
    }

    public int getImmunityDuration() {
        return immunityDuration;
    }

    public int getInfectionDuration() {
        return infectionDuration;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public ArrayList<Cell> getInitialDistribution() {
        return initialDistribution;
    }

    public CalculationMode getMode() {
        return mode;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "totalWidth=" + totalWidth +
                ", totalHeight=" + totalHeight +
                ", x0=" + x0 +
                ", x1=" + x1 +
                ", y0=" + y0 +
                ", y1=" + y1 +
                ", S=" + s +
                ", I=" + i +
                ", R=" + r +
                ", beta=" + beta +
                ", immunityDuration=" + immunityDuration +
                ", infectionDuration=" + infectionDuration +
                ", maxSteps=" + maxSteps +
                ", initialDistribution=" + initialDistribution +
                '}';
    }

    private static boolean isValidInput(JSONObject jsonObject) {
        String[] params = {"totalWidth", "totalHeight", "x0", "x1", "y0", "y1", "S", "I", "R", "durations", "immunityDuration", "infectionDuration", "maxSteps", "beta", "initialDistribution", "mode"};
        try {
            for (String param : params) {
                jsonObject.get(param);
            }
            return true;
        } catch (JSONException jE) {
            System.out.println(jE.getMessage());
            return false;
        }
    }


    private static Configuration constructFromJson(JSONObject jsonObject) {
        int totalWidth, totalHeight, x0, x1, y0, y1, s, i, r, immunityDuration, infectionDuration, maxSteps;
        double beta;
        ArrayList<Cell> initialDistribution = new ArrayList<>();
        CalculationMode mode;

        totalWidth = jsonObject.getInt("totalWidth");
        if (totalWidth <= 0) totalWidth = TOTAL_WIDTH;
        totalHeight = jsonObject.getInt("totalHeight");
        if (totalHeight <= 0) totalHeight = TOTAL_HEIGHT;
        x0 = jsonObject.getInt("x0");
        if (x0 < 0 || x0 > totalWidth) x0 = X_0;
        x1 = jsonObject.getInt("x1");
        if (x1 < 1 || x1 > totalWidth) x1 = X_1;
        y0 = jsonObject.getInt("y0");
        if (y0 < 0 || y0 > totalHeight) y0 = Y_0;
        y1 = jsonObject.getInt("y1");
        if (y1 < 1 || y1 > totalHeight) y1 = Y_1;
        s = jsonObject.getInt("S");
        if (s < 0) s = S;
        i = jsonObject.getInt("I");
        if (i < 0) i = I;
        r = jsonObject.getInt("R");
        if (r < 0) r = R;
        if (DurationsMode.valueOf(jsonObject.getString("durations")) == DurationsMode.FIXED) {
            immunityDuration = jsonObject.getInt("immunityDuration");
            if (immunityDuration < 0) immunityDuration = ThreadLocalRandom.current().nextInt(30, 40 + 1);
            infectionDuration = jsonObject.getInt("infectionDuration");
            if (infectionDuration < 0) infectionDuration = ThreadLocalRandom.current().nextInt(30, 40 + 1);
        } else {
            immunityDuration = ThreadLocalRandom.current().nextInt(30, 40 + 1);
            infectionDuration = ThreadLocalRandom.current().nextInt(30, 40 + 1);
        }
        maxSteps = jsonObject.getInt("maxSteps");
        if (maxSteps < 0) maxSteps = MAX_STEPS;
        beta = jsonObject.getDouble("beta");
        if (beta < 0) beta = BETA;

        for (Object o : jsonObject.getJSONArray("initialDistribution")) {
            Cell helperCell = new Cell();
            JSONObject jO = (JSONObject) o;
            helperCell.setX(jO.getInt("x"));
            helperCell.setY(jO.getInt("y"));
            helperCell.setStatus(CellStatus.valueOf(jO.getString("status")));

            initialDistribution.add(helperCell);
        }
        mode = CalculationMode.valueOf(jsonObject.getString("mode"));

        return new Configuration(totalWidth, totalHeight, x0, x1, y0, y1, s, i, r, beta, immunityDuration, infectionDuration, maxSteps, initialDistribution, mode);
    }

    public static Configuration createFromJson(JSONObject jsonObject) {
        if (!Configuration.isValidInput(jsonObject))
            return new Configuration(TOTAL_WIDTH, TOTAL_HEIGHT, X_0, X_1, Y_0, Y_1, S, I, R, BETA, ThreadLocalRandom.current().nextInt(30, 40 + 1), ThreadLocalRandom.current().nextInt(30, 40 + 1), MAX_STEPS, new ArrayList<Cell>(), MODE);

        return Configuration.constructFromJson(jsonObject);
    }
}
