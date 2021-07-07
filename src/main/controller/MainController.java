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

public class MainController implements IController {
    private MainView mainView;
    private EpidemicState model;
    private Configuration configuration;
    private volatile Boolean isRunning;

    public MainController(Configuration configuration, EpidemicState model) {
        this.mainView = new MainView(model, this, configuration.getX0(), configuration.getX1(), configuration.getY0(), configuration.getY1(), model.getCurrentField());
        this.model = model;
        this.configuration = configuration;
        this.mainView.updateLabel(model.getCurrentStep(), model.getCurrentSusceptibleCount(), model.getCurrentInfectedCount(), model.getCurrentRecoveredCount());
        isRunning = false;
    }

    public IView getView() {
        return this.mainView;
    }

    @Override
    public void start() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                isRunning = true;
                while (configuration.getMaxSteps() > 0 ? (model.getCurrentStep() < configuration.getMaxSteps() && isRunning) : isRunning) {
                    model.handleIteration();
                    mainView.updateImage(model.getCurrentField());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            mainView.updateLabel(model.getCurrentStep(), model.getCurrentSusceptibleCount(), model.getCurrentInfectedCount(), model.getCurrentRecoveredCount());
                        }
                    });
                }
            }
        });
        t.setName("Backend Thread");
        t.start();
    }

    @Override
    public void stop() {
        isRunning = false;
        System.out.println(Thread.currentThread().getName());
    }

    @Override
    public void reset() {
        try {
            stop();
            JSONObject jsonInput = JsonLoader.getData();
            Configuration configuration = Configuration.createFromJson(jsonInput);
            this.configuration = configuration;
            model.initState(configuration);
            mainView.resetImage(configuration.getX0(), configuration.getX1(), configuration.getY0(), configuration.getY1(), model.getCurrentField());
//            mainView.updateLabel(model.getCurrentStep(), model.getCurrentSusceptibleCount(), model.getCurrentInfectedCount(), model.getCurrentRecoveredCount());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
