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
    private static final int COLUMNS = 100;
    int[][] field = new int[COLUMNS][ROWS];
    int[][] rField = new int[COLUMNS][ROWS];
    private static int S_COUNT = 6000;
    private static int I_COUNT = 1;
    private static int R_COUNT = 0;
    private static final double BETA = 0.33;
    private static final int R_TIME = 100;
    private static final int I_TIME = 30;

    //  # UI Params
    private static final int UI_ROWS_START = 0;
    private static final int UI_COLUMNS_START = 0;
    private static final int UI_ROWS_END = 100;
    private static final int UI_COLUMNS_END = 100;
    private static final int CELL_SIZE = 2;

    private BorderPane rootPane = new BorderPane();
    private Pane boardPane;
    private Label label;
    private Board board = new Board(CELL_SIZE);
    private Map<String, Integer> fieldMap = new HashMap<>();
    private static int step = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize base layout
        label = new Label(
                "Step: 0, N = " + COLUMNS * ROWS
                        + ", S: " + S_COUNT
                        + ", I: " + I_COUNT
                        + " , R: " + R_COUNT);
        label.setPrefHeight(20);
        rootPane.setTop(label);
        HBox hBox = new HBox();
        Button bStart = new Button("Start");
        Button bPause = new Button("Pause");
        Button bReset = new Button("Reset");
        hBox.getChildren().add(bStart);
        hBox.getChildren().add(bPause);
        hBox.getChildren().add(bReset);
        rootPane.setBottom(hBox);
        hBox.setPrefHeight(40);
        hBox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(rootPane, (UI_COLUMNS_END - UI_COLUMNS_START) * CELL_SIZE, (UI_ROWS_END - UI_ROWS_START) * CELL_SIZE + label.getPrefHeight() + hBox.getPrefHeight());
        scene.getStylesheets().add("index.css");


        // Initialize field with empty cells
        initField();

        // randomly place cells
        randPerm(S_COUNT, 1);
        randPerm(I_COUNT, 2);

        boardPane = board.generate(field, UI_COLUMNS_START, UI_COLUMNS_END, UI_ROWS_START, UI_ROWS_END);
        rootPane.setCenter(boardPane);

        final Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new EventHandler() {
            @Override
            public void handle(Event actionEvent) {
                calc_step();
                calc_recovery();
                calc_infection();
                updateLabel(step);
                step++;
                board.update(boardPane, field, fieldMap, UI_COLUMNS_START, UI_COLUMNS_END, UI_ROWS_START, UI_ROWS_END);
            }
        }), new KeyFrame(Duration.millis(100)));
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

        bReset.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                timeline.pause();
                resetData();
            }
        });

        // set scene and start the program
        primaryStage.setTitle("PVR Krankheitsausbreitung");
        primaryStage.setScene(scene);
//        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void randPerm(int count, int status) {
        for (int c = 0; c < count; c++) {
            int x = ThreadLocalRandom.current().nextInt(0, COLUMNS);
            int y = ThreadLocalRandom.current().nextInt(0, ROWS);
            field[x][y] = status;
        }
    }

    private void initField() {
        int indexCounter = 0;
        for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                field[x][y] = 0;
                rField[x][y] = R_TIME;

                fieldMap.put(x + ";" + y, indexCounter);
                indexCounter++;
            }
        }
    }

    private void move(int x1, int y1, int x2, int y2) {
        int h = field[x1][y1];
        field[x1][y1] = field[x2][y2];
        field[x2][y2] = h;

        h = rField[x1][y1];
        rField[x1][y1] = rField[x2][y2];
        rField[x2][y2] = h;
    }

    private void calc_step() {
        int t;
        int x2 = 0;
        int y2 = 0;

        for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                if (field[x][y] > 0) {
                    t = ThreadLocalRandom.current().nextInt(0, 8);

                    if (t == 0) {
                        x2 = xPos(x - 1);
                        y2 = yPos(y - 1);
                    } else if (t == 1) {
                        x2 = x;
                        y2 = yPos(y - 1);
                    } else if (t == 2) {
                        x2 = xPos(x + 1);
                        y2 = yPos(y - 1);
                    } else if (t == 3) {
                        x2 = xPos(x + 1);
                        y2 = y;
                    } else if (t == 4) {
                        x2 = xPos(x + 1);
                        y2 = yPos(y + 1);
                    } else if (t == 5) {
                        x2 = x;
                        y2 = yPos(y + 1);
                    } else if (t == 6) {
                        x2 = xPos(x - 1);
                        y2 = yPos(y + 1);
                    } else if (t == 7) {
                        x2 = xPos(x - 1);
                        y2 = y;
                    }

                    if (field[x2][y2] == 0) {
                        // frei?
                        move(x, y, x2, y2);
                    }
                }
            }
        }
    }

    private void calc_infection() {
        for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                if (field[x][y] == 1) {

                    boolean getIll = false;
                    if (field[xPos(x - 1)][yPos(y - 1)] == 2 && Math.random() < BETA) {
                        getIll = true;
                    }
                    if (field[x][yPos(y - 1)] == 2 && Math.random() < BETA) {
                        getIll = true;
                    }
                    if (field[xPos(x + 1)][yPos(y - 1)] == 2 && Math.random() < BETA) {
                        getIll = true;
                    }
                    if (field[xPos(x + 1)][y] == 2 && Math.random() < BETA) {
                        getIll = true;
                    }
                    if (field[xPos(x + 1)][yPos(y + 1)] == 2 && Math.random() < BETA) {
                        getIll = true;
                    }
                    if (field[x][yPos(y + 1)] == 2 && Math.random() < BETA) {
                        getIll = true;
                    }
                    if (field[xPos(x - 1)][yPos(y + 1)] == 2 && Math.random() < BETA) {
                        getIll = true;
                    }
                    if (field[xPos(x - 1)][y] == 2 && Math.random() < BETA) {
                        getIll = true;
                    }

                    if (getIll == true) {
                        field[x][y] = 2;
                        rField[x][y] = R_TIME;
                    }
                }
            }
        }
    }

    private void calc_recovery() {
        for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                if (field[x][y] == 2) {
                    rField[x][y] = rField[x][y] - 1;
                    if (rField[x][y] <= 0) {
                        field[x][y] = 3;
                        rField[x][y] = I_TIME;
                    }
                } else if (field[x][y] == 3) {
                    rField[x][y] = rField[x][y] - 1;
                    if (rField[x][y] <= 0) {
                        field[x][y] = 1;
                        rField[x][y] = 0;
                    }
                }
            }
        }
    }

    private int xPos(int x) {
        int x2 = x;
        if (x2 == -1) x2 = COLUMNS - 1;
        if (x2 == COLUMNS) x2 = 0;
        return x2;
    }

    private int yPos(int y) {
        int y2 = y;
        if (y2 == -1) y2 = ROWS - 1;
        if (y2 == ROWS) y2 = 0;
        return y2;
    }

    private void updateLabel(int step) {
        S_COUNT = 0;
        I_COUNT = 0;
        R_COUNT = 0;
        for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                if (field[x][y] == 1) S_COUNT++;
                if (field[x][y] == 2) I_COUNT++;
                if (field[x][y] == 3) R_COUNT++;
            }
        }
        label.setText("Step: " + step + ", N = " + COLUMNS * ROWS
                + ", S: " + S_COUNT
                + ", I: " + I_COUNT
                + ", R: " + R_COUNT);
    }

    private void resetData() {
        S_COUNT = 6000;
        I_COUNT = 1;
        R_COUNT = 0;
        step = 0;
        fieldMap = new HashMap<>();
        initField();
        randPerm(S_COUNT, 1);
        randPerm(I_COUNT, 2);
        label.setText("Step: 0, N = " + COLUMNS * ROWS
                + ", S: " + S_COUNT
                + ", I: " + I_COUNT
                + " , R: " + R_COUNT);
        boardPane = board.generate(field, UI_COLUMNS_START, UI_COLUMNS_END, UI_ROWS_START, UI_ROWS_END);
        rootPane.setCenter(boardPane);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
