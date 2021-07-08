package main.view;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import main.controller.IController;
import main.model.CellStatus;
import main.model.EpidemicState;

import java.text.DecimalFormat;

public class MainView implements IView {
    private IController mainController;
    private Scene scene;
    private BorderPane rootPane;
    private ImageView imageView;
    private WritableImage img;
    private PixelWriter pixelWriter;
    private Label label;
    private Button bStart;
    private Button bPause;
    private Button bReset;
    private int x0, x1, y0, y1;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    public MainView(EpidemicState model, IController controller, int x0, int x1, int y0, int y1, byte[][] field) {
        this.mainController = controller;
        this.rootPane = new BorderPane();

        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
        this.imageView = new ImageView();
        this.img = new WritableImage(x1 - x0, y1 - y0);
        this.pixelWriter = img.getPixelWriter();
        updateImage(field);
        imageView.setImage(img);

        this.label = new Label("Label");
        label.setPrefHeight(20);

        HBox hBox = new HBox();
        Button bStart = new Button("Start/Stop");
        Button bReset = new Button("Reload Params and Reset");
        bStart.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mainController.toggleLoop();
            }
        });
        bReset.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mainController.reset();
            }
        });
        hBox.getChildren().addAll(bStart, bReset);
        hBox.setPrefHeight(40);
        hBox.setAlignment(Pos.CENTER);

        rootPane.setTop(label);
        rootPane.setCenter(imageView);
        rootPane.setBottom(hBox);
        scene = new Scene(rootPane, x1 - x0, y1 - y0 + label.getPrefHeight() + hBox.getPrefHeight());
    }

    @Override
    public void updateImage(byte[][] newField) {
        int xHelper = 0;
        for (int x = x0; x < x1; x++) {
            if (xHelper < x1 - x0) {
                int yHelper = 0;
                for (int y = y0; y < y1; y++) {
                    if (yHelper < y1 - y0) {
                        if (newField[y][x] == 0) pixelWriter.setColor(xHelper, yHelper, Color.BLACK);
                        if (newField[y][x] == 1)
                            pixelWriter.setColor(xHelper, yHelper, Color.WHITE);
                        if (newField[y][x] == 2) pixelWriter.setColor(xHelper, yHelper, Color.RED);
                        if (newField[y][x] == 3) pixelWriter.setColor(xHelper, yHelper, Color.BLUE);

                        yHelper++;
                    }
                }
                xHelper++;
            }
        }
    }

    @Override
    public void updateLabel(int step, int initialCellCount, int sCount, int iCount, int rCount) {
        double nonEmptyCellCount = sCount + iCount + rCount;
        String newText = "N = " + initialCellCount
                + " | Step: " + step
                + " | S: " + DECIMAL_FORMAT.format((sCount / nonEmptyCellCount) * 100) + "%"
                + " | I: " + DECIMAL_FORMAT.format((iCount / nonEmptyCellCount) * 100) + "%"
                + " | R: " + DECIMAL_FORMAT.format((rCount / nonEmptyCellCount) * 100) + "%";
        label.setText(newText);
    }

    private void setDimensions(int x0, int x1, int y0, int y1) {
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
    }

    @Override
    public void resetImage(int x0, int x1, int y0, int y1, byte[][] field) {
        this.setDimensions(x0, x1, y0, y1);
        this.imageView = new ImageView();
        this.img = new WritableImage(x1 - x0, y1 - y0);
        this.pixelWriter = img.getPixelWriter();
        updateImage(field);
        imageView.setImage(img);
        rootPane.setCenter(imageView);
    }

    @Override
    public Scene getScene() {
        return scene;
    }
}
