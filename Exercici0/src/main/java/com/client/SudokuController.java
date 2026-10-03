package com.client;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class SudokuController {

    @FXML
    private GridPane gridSudoku;

    @FXML
    private javafx.scene.control.Label lblPlayer;

    private final Button[][] cells =
            new Button[9][9];

    public void setPlayerName(String name) {

        lblPlayer.setText(
                "Juega: " + name
        );
    }

    public void updateBoard(
            JSONObject board) {

        JSONArray puzzle =
                board.getJSONArray("puzzle");

        gridSudoku.getChildren().clear();

        /*
         * Crear las 81 casillas.
         */
        for (int row = 0; row < 9; row++) {

            for (int col = 0; col < 9; col++) {

                int value =
                        puzzle
                                .getJSONArray(row)
                                .getInt(col);

                Button button =
                        new Button();

                button.setPrefSize(
                        55,
                        55
                );

                cells[row][col] = button;

                if (value != 0) {

                    button.setText(
                            String.valueOf(value)
                    );

                    button.setDisable(true);

                    button.setStyle(
                            "-fx-background-color: #dddddd;"
                    );

                } else {

                    final int r = row;
                    final int c = col;

                    button.setOnAction(
                            event ->
                                    askForValue(r, c)
                    );
                }

                gridSudoku.add(
                        button,
                        col,
                        row
                );
            }
        }

        /*
         * Pintamos las casillas que algún jugador
         * ya ha acertado.
         */
        JSONArray solved =
                board.getJSONArray("solved");

        for (int i = 0;
             i < solved.length();
             i++) {

            JSONObject cell =
                    solved.getJSONObject(i);

            int row =
                    cell.getInt("row");

            int col =
                    cell.getInt("col");

            int value =
                    cell.getInt("value");

            Button button =
                    cells[row][col];

            button.setText(
                    String.valueOf(value)
            );

            button.setDisable(true);

            button.setStyle(
                    "-fx-background-color: #90EE90;"
                    + "-fx-text-fill: green;"
                    + "-fx-font-weight: bold;"
            );
        }
    }

    private void askForValue(
            int row,
            int col) {

        TextInputDialog dialog =
                new TextInputDialog();

        dialog.setTitle("Sudoku");

        dialog.setHeaderText(
                "Casilla "
                        + (row + 1)
                        + ", "
                        + (col + 1)
        );

        dialog.setContentText(
                "Introduce un número del 1 al 9:"
        );

        Optional<String> result =
                dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        try {

            int value =
                    Integer.parseInt(
                            result.get().trim()
                    );

            if (value < 1 || value > 9) {

                showMessage(
                        "El número debe estar entre 1 y 9."
                );

                return;
            }

            Main.instance.sendMove(
                    row,
                    col,
                    value
            );

        } catch (NumberFormatException e) {

            showMessage(
                    "Introduce solamente un número."
            );
        }
    }

    public void showMessage(
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle("Sudoku");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}