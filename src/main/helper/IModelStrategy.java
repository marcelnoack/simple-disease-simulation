package main.helper;

public interface IModelStrategy {
    public void calcIteration(int totalWidth, int totalHeight, byte[][] field, short[][] rField, double beta, short immunityDuration, short infectionDuration);
    public void calcStep(int totalWidth, int totalHeight, byte[][] field, short[][] rField);
    public void calcRecovery(int totalWidth, int totalHeight, byte[][] field, short[][] rField,
            short immunityDuration);
    public void calcInfection(int totalWidth, int totalHeight, double beta, byte[][] field, short[][] rField,
            short infectionDuration);
}
