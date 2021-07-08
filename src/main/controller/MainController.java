package main.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.util.Duration;
import main.JsonLoader;
import main.model.Configuration;
import main.model.EpidemicState;
import main.view.IView;
import main.view.MainView;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class MainController implements IController {
    private MainView mainView;
    private EpidemicState model;
    private Configuration configuration;
    private volatile Boolean isRunning;

    public MainController(Configuration configuration, EpidemicState model) {
        this.mainView = new MainView(model, this, configuration.getX0(), configuration.getX1(), configuration.getY0(), configuration.getY1(), model.getCurrentField());
        this.model = model;
        this.configuration = configuration;
        this.mainView.updateLabel(model.getCurrentStep(), configuration.getS() + configuration.getI() + configuration.getR(), model.getCurrentSusceptibleCount(), model.getCurrentInfectedCount(), model.getCurrentRecoveredCount());
        isRunning = false;
    }

    public IView getView() {
        return this.mainView;
    }

    @Override
    public void toggleLoop() {
        isRunning = !isRunning;
        if (isRunning) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
//                isRunning = true;
                    while (configuration.getMaxSteps() > 0 ? (model.getCurrentStep() < configuration.getMaxSteps() && isRunning) : isRunning) {
                        long t1 = System.currentTimeMillis();
                        model.handleIteration();
                        long t2 = System.currentTimeMillis();
                        FutureTask updateUI = new FutureTask(new Callable() {
                            @Override
                            public Object call() throws Exception {
                                mainView.updateImage(model.getCurrentField());
                                mainView.updateLabel(model.getCurrentStep(), configuration.getS() + configuration.getI() + configuration.getR(), model.getCurrentSusceptibleCount(), model.getCurrentInfectedCount(), model.getCurrentRecoveredCount());
                                return null;
                            }
                        });
                        Platform.runLater(updateUI);
                        try {
                            updateUI.get();
                            long t3 = System.currentTimeMillis();
                            System.out.println("-----------");
                            System.out.println("Berechnung:" + (t2 - t1));
                            System.out.println("FX: " + (t3 - t2));
                            System.out.println("Gesamt: " + (t3 - t1));
                            System.out.println("-----------");
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            t.setName("Backend Thread");
            t.start();
        }
    }

    @Override
    public void reset() {
        try {
            isRunning = false;
            JSONObject jsonInput = JsonLoader.getData();
            Configuration configuration = Configuration.createFromJson(jsonInput);
            this.configuration = configuration;
            Thread.sleep(50);
            model.initState(configuration);
            mainView.resetImage(configuration.getX0(), configuration.getX1(), configuration.getY0(), configuration.getY1(), model.getCurrentField());
            mainView.updateLabel(model.getCurrentStep(), configuration.getS() + configuration.getI() + configuration.getR(), model.getCurrentSusceptibleCount(), model.getCurrentInfectedCount(), model.getCurrentRecoveredCount());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
