package unstructured;

import javafx.scene.layout.Pane;

import java.util.Map;

public class Board {
    private int cellSize;

    Board(int cellSize) {
        this.cellSize = cellSize;
    }

//   public Pane generate(int[][] field) {
//       Pane boardPane = new Pane();
//
//       for(int x = 0; x < cellSize * field.length; x += cellSize) {
//           for(int y = 0; y < cellSize * field[0].length; y += cellSize) {
//               boardPane.getChildren().add(generateCell(x, y, field));
//           }
//       }
//
//       return boardPane;
//   }

    public Pane generate(int[][] field, int startX, int endX, int startY, int endY) {
        Pane boardPane = new Pane();
        int xUI = 0;
        int yUI;

        for (int x = startX * cellSize; x < endX * cellSize; x += cellSize) {
            if (x != startX * cellSize) xUI += cellSize;
            yUI = 0;
            for (int y = startY * cellSize; y < endY * cellSize; y += cellSize) {
                if (y != startY * cellSize) yUI += cellSize;
                boardPane.getChildren().add(generateCell(x, y, field, xUI, yUI));
            }
        }
        return boardPane;
    }

    private Pane generateCell(int x, int y, int[][] field, int xUI, int yUI) {
        Pane cell = new Pane();
        cell.setLayoutX(xUI);
        cell.setLayoutY(yUI);
        cell.setPrefWidth(cellSize);
        cell.setPrefHeight(cellSize);
        if (field[x / cellSize][y / cellSize] == 0) cell.getStyleClass().add("empty-cell");
        if (field[x / cellSize][y / cellSize] == 1) cell.getStyleClass().add("susceptible-cell");
        if (field[x / cellSize][y / cellSize] == 2) cell.getStyleClass().add("infectious-cell");
        if (field[x / cellSize][y / cellSize] == 3) cell.getStyleClass().add("recovered-cell");

        return cell;
    }

    public void update(Pane boardPane, int[][] newField, Map<String, Integer> fieldMap, int startX, int endX, int startY, int endY) {
        for (int i = 0; i < boardPane.getChildren().size(); i++) {
            boardPane.getChildren().get(i).getStyleClass().clear();
        }
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                if(newField[x][y] == 0) boardPane.getChildren().get(fieldMap.get(x + ";" + y)).getStyleClass().add("empty-cell");
                if(newField[x][y] == 1) boardPane.getChildren().get(fieldMap.get(x + ";" + y)).getStyleClass().add("susceptible-cell");
                if(newField[x][y] == 2) boardPane.getChildren().get(fieldMap.get(x + ";" + y)).getStyleClass().add("infectious-cell");
                if(newField[x][y] == 3) boardPane.getChildren().get(fieldMap.get(x + ";" + y)).getStyleClass().add("recovered-cell");
            }
        }
    }
}
