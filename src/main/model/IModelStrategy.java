package main.model;

public interface IModelStrategy {
    public void calcStep(Configuration configuration, CellStatus[][] field, int[][] rField);
    public void calcRecovery(int totalWidth, int totalHeight, CellStatus[][] field, int[][] rField, int immunityDuration);
    public void calcInfection(int totalWidth, int totalHeight, double beta, CellStatus[][] field, int[][] rField, int immunityDuration);
}