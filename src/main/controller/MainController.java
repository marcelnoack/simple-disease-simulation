package main.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
//    private Timeline timeline;

    public MainController(Configuration configuration, EpidemicState model) {
        this.mainView = new MainView(this, configuration.getX0(), configuration.getX1(), configuration.getY0(), configuration.getY1(), model.getCurrentField());
        this.model = model;
        this.configuration = configuration;
        this.mainView.updateLabel(model.getCurrentStep(), model.getCurrentSusceptibleCount(), model.getCurrentInfectedCount(), model.getCurrentRecoveredCount());
        isRunning = false;
        //        setupLoop();
    }

//    private void setupLoop() {
//        timeline = new Timeline(new KeyFrame(Duration.ZERO, new EventHandler() {
//            @Override
//            public void handle(Event actionEvent) {
//                model.handleIteration();
//                mainView.updateImage(model.getCurrentField());
//                mainView.updateLabel(model.getCurrentStep(), model.getCurrentSusceptibleCount(), model.getCurrentInfectedCount(), model.getCurrentRecoveredCount());
//            }
//        }), new KeyFrame(Duration.millis(50)));
//        if (configuration.getMaxSteps() > 0) {
//            timeline.setCycleCount(configuration.getMaxSteps());
//        } else {
//            timeline.setCycleCount(Timeline.INDEFINITE);
//        }
//    }

    public IView getView() {
        return this.mainView;
    }

    @Override
    public void start() {
//        timeline.play();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                isRunning = true;
                while (isRunning) {
                    long start = System.currentTimeMillis();
                    model.handleIteration();
                    long end = System.currentTimeMillis();
                    System.out.println(end - start);
                    mainView.updateImage(model.getCurrentField());
//                    mainView.updateLabel(model.getCurrentStep(), model.getCurrentSusceptibleCount(), model.getCurrentInfectedCount(), model.getCurrentRecoveredCount());
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
            mainView.updateLabel(model.getCurrentStep(), model.getCurrentSusceptibleCount(), model.getCurrentInfectedCount(), model.getCurrentRecoveredCount());
//            setupLoop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
