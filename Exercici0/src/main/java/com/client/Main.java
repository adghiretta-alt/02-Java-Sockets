package com.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.json.JSONObject;

public class Main extends Application {

    public static Main instance;

    private Stage stage;

    private ConfigController configController;
    private PlayersController playersController;
    private SudokuController sudokuController;

    private WebSocketService webSocket;

    private String playerName;

    @Override
    public void start(Stage stage) throws Exception {

        instance = this;

        this.stage = stage;

        showConfig();

        stage.setTitle("Sudoku Multijugador");

        stage.show();
    }

    private void showConfig() throws Exception {

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/assets/config.fxml"
                        )
                );

        Parent root = loader.load();

        configController =
                loader.getController();

        Scene scene = new Scene(
                root,
                500,
                400
        );

        stage.setScene(scene);
    }

    public void connect(
            String host,
            int port,
            String name) {

        playerName = name;

        webSocket = new WebSocketService(
                host,
                port,
                name,
                this::receiveMessage
        );
    }

    private void receiveMessage(String message) {

        Platform.runLater(() -> {

            try {

                JSONObject obj =
                        new JSONObject(message);

                String type =
                        obj.optString("type");

                switch (type) {

                    case "registered":

                        showPlayers();
                        break;

                    case "players":

                        if (playersController != null) {

                            playersController
                                    .updatePlayers(
                                            obj.getJSONArray(
                                                    "players"
                                            )
                                    );
                        }

                        break;

                    case "board":

                        if (sudokuController != null) {

                            sudokuController
                                    .updateBoard(obj);
                        }

                        break;

                    case "wrong":

                        if (sudokuController != null) {

                            sudokuController
                                    .showMessage(
                                            obj.getString(
                                                    "message"
                                            )
                                    );
                        }

                        break;

                    case "error":

                        if (configController != null) {

                            configController
                                    .showError(
                                            obj.getString(
                                                    "message"
                                            )
                                    );
                        }

                        break;
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        });
    }

    public void showPlayers() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/assets/players.fxml"
                            )
                    );

            Parent root = loader.load();

            playersController =
                    loader.getController();

            playersController
                    .setPlayerName(playerName);

            stage.setScene(
                    new Scene(
                            root,
                            600,
                            500
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void showSudoku() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/assets/sudoku.fxml"
                            )
                    );

            Parent root = loader.load();

            sudokuController =
                    loader.getController();

            sudokuController
                    .setPlayerName(playerName);

            stage.setScene(
                    new Scene(
                            root,
                            900,
                            650
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void sendMove(
            int row,
            int col,
            int value) {

        if (webSocket != null) {

            webSocket.sendMove(
                    row,
                    col,
                    value
            );
        }
    }

    @Override
    public void stop() {

        if (webSocket != null) {
            webSocket.close();
        }
    }

    public static void main(String[] args) {

        launch(args);
    }
}