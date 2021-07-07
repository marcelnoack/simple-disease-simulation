package main.model;

public interface IModelStrategy {
    public void calcIteration(int totalWidth, int totalHeight, CellStatus[][] field, int[][] rField, double beta, int immunityDuration, int infectionDuration);
    public void calcStep(int totalWidth, int totalHeight, CellStatus[][] field, int[][] rField);

    public void calcRecovery(int totalWidth, int totalHeight, CellStatus[][] field, int[][] rField,
            int immunityDuration);

    public void calcInfection(int totalWidth, int totalHeight, double beta, CellStatus[][] field, int[][] rField,
            int infectionDuration);
}
