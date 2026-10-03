package com.client;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class PlayersController {

    @FXML
    private Label lblPlayer;

    @FXML
    private ListView<String> lstPlayers;

    public void setPlayerName(String name) {

        lblPlayer.setText(
                "Jugador: " + name
        );
    }

    public void updatePlayers(
            JSONArray players) {

        lstPlayers.getItems().clear();

        for (int i = 0;
             i < players.length();
             i++) {

            JSONObject player =
                    players.getJSONObject(i);

            String name =
                    player.getString("name");

            int score =
                    player.getInt("score");

            lstPlayers.getItems().add(
                    name + "    " + score + " puntos"
            );
        }
    }

    @FXML
    private void play() {

        Main.instance.showSudoku();
    }
}