package main.model.calculation;

import main.model.types.CalculationMode;
import main.model.types.EpidemicModel;

public class ModelFactory {
    public static IModelStrategy getModelStrategy(CalculationMode mode, EpidemicModel model) {
        // default strategy
        IModelStrategy modelStrategy = new SequentialSIRS();

        if(mode == CalculationMode.SEQUENTIAL && model == EpidemicModel.SIRS) modelStrategy = new SequentialSIRS();
        if(mode == CalculationMode.PARALLEL && model == EpidemicModel.SIRS) modelStrategy = new ParallelSIRS();
        if(mode == CalculationMode.SEQUENTIAL && model == EpidemicModel.SIS) {
            // will not be implemented due to single assignment; returning SequentialSIRS by default
        }
        if(mode == CalculationMode.PARALLEL && model == EpidemicModel.SIS) {
            // will not be implemented due to single assignment; returning SequentialSIRS by default
        }

        return modelStrategy;
    }
}
