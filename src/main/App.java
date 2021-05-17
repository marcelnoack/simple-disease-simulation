package main;

import javafx.application.Application;
import javafx.stage.Stage;
import main.controller.MainController;
import main.model.Configuration;
import main.model.EpidemicState;
import org.json.JSONObject;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        JSONObject jsonInput = JsonLoader.getData();
        Configuration configuration = Configuration.createFromJson(jsonInput);

        EpidemicState epidemicState = new EpidemicState(configuration);
        MainController mainController = new MainController(configuration, epidemicState);

        primaryStage.setTitle("PVR Krankheitsausbreitung");
        primaryStage.setScene(mainController.getView().getScene());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
