package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


public class Main extends Application {

    // Temporary start params --> will be read dynamically later on
    private static final int ROWS = 100;
    private static final int COLUMNS = 200;
    private static final int S_COUNT = 8500;
    private static final int I_COUNT = 1;
    private static final int R_COUNT = 0;
    int[][] field = new int[COLUMNS][ROWS];

    //  # UI Params
    private static final int UI_ROWS_START = 0;
    private static final int UI_COLUMNS_START = 0;
    private static final int UI_ROWS_END = 100;
    private static final int UI_COLUMNS_END = 100;
    private static final int CELL_SIZE = 4;

    private Pane boardPane;
    private Map<String, Integer> fieldMap = new HashMap<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize base layout
        BorderPane rootPane = new BorderPane();
        Label label = new Label(
                "N = " + COLUMNS * ROWS
                        + ", S: " + ((float) S_COUNT) / (S_COUNT + I_COUNT + R_COUNT) * 100
                        + "%, I: " + ((float) I_COUNT) / (S_COUNT + I_COUNT + R_COUNT) * 100
                        + " %, R: " + ((float) R_COUNT) / (S_COUNT + I_COUNT + R_COUNT) * 100 + "%");
        label.setPrefHeight(20);
        rootPane.setTop(label);
        HBox hBox = new HBox();
        Button bStart = new Button("Start");
        Button bPause = new Button("Pause");
        hBox.getChildren().add(bStart);
        hBox.getChildren().add(bPause);
        rootPane.setBottom(hBox);
        hBox.setPrefHeight(40);
        hBox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(rootPane, (UI_COLUMNS_END - UI_COLUMNS_START) * CELL_SIZE, (UI_ROWS_END - UI_ROWS_START) * CELL_SIZE + label.getPrefHeight() + hBox.getPrefHeight());
        scene.getStylesheets().add("index.css");


        // Initialize field with empty cells
        initField(field);

        // randomly place cells
        randPerm(field, S_COUNT, 1);
        randPerm(field, I_COUNT, 2);

        Board board = new Board(CELL_SIZE);
        boardPane = board.generate(field, UI_COLUMNS_START, UI_COLUMNS_END, UI_ROWS_START, UI_ROWS_END);
        rootPane.setCenter(boardPane);
        System.out.println(boardPane.getChildren().size());
        System.out.println(boardPane.getChildren().get(0).getAccessibleText());

        final Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new EventHandler() {
            @Override
            public void handle(Event actionEvent) {
                field = nextIteration(field);
                board.update(boardPane, field, fieldMap, UI_COLUMNS_START, UI_COLUMNS_END, UI_ROWS_START, UI_ROWS_END);
            }
        }), new KeyFrame(Duration.millis(50)));
        timeline.setCycleCount(Timeline.INDEFINITE);

        bStart.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                timeline.play();
            }
        });

        bPause.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                timeline.pause();
            }
        });

        // set scene and start the program
        primaryStage.setTitle("PVR Krankheitsausbreitung");
        primaryStage.setScene(scene);
//        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void randPerm(int[][] field, int count, int status) {
        for (int c = 0; c < count; c++) {
            int x = ThreadLocalRandom.current().nextInt(0, COLUMNS);
            int y = ThreadLocalRandom.current().nextInt(0, ROWS);
            field[x][y] = status;
        }
    }

    private void initField(int[][] field) {
        int indexCounter = 0;
        for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                field[x][y] = 0;

                fieldMap.put(x + ";" + y, indexCounter);
                indexCounter++;
            }
        }
    }

    private int[][] nextIteration(int[][] field) {
        int[][] newField = new int[COLUMNS][ROWS];
        for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                newField[x][y] = field[x][y];
                checkCell(x, y, field, newField);
            }
        }
        return newField;
    }

    private void checkCell(int x, int y, int[][] field, int[][] newField) {
        int[] indexX = {-1, 0, 1, 1, 1, 0, -1, -1};
        int[] indexY = {1, 1, 1, 0, -1, -1, -1, 0};

        int fieldValue = field[x][y];

        int neighbours = 0;
        for(int i = 0; i < 8; i++) {
            if(x + indexX[i] >= 0 && y+indexY[i] >=0 && x + indexX[i] < COLUMNS && y + indexY[i] < ROWS) {
                neighbours = neighbours + field[x + indexX[i]][y + indexY[i]];
            }
        }

        if(fieldValue == 0 && neighbours == 3) {
            // setField(x, y, 1); // Reborn with three alive neighbours
            newField[x][y] = 1;
            return;
        }

        if(fieldValue == 1 && neighbours < 2) {
            // setField(x, y, 0); // Less than two alive neighbours die
            newField[x][y] = 0;
            return;
        }

        if(fieldValue == 1 && (neighbours == 2 || neighbours == 3)) {
            // Stay alive if two or three alive neighbours
            return;
        }

        if(fieldValue == 1 && neighbours > 3) {
            // setField(x, y, 0); // Die if more than three alive neighbours
            newField[x][y] = 0;
            return;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
