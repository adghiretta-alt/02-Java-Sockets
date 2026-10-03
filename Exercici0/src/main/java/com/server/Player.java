package com.server;

import org.java_websocket.WebSocket;

public class Player {

    private final WebSocket socket;
    private final String name;

    private int score;

    public Player(WebSocket socket, String name) {
        this.socket = socket;
        this.name = name;
        this.score = 0;
    }

    public WebSocket getSocket() {
        return socket;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void addPoints(int points) {
        score += points;
    }
}