package com.client;

import java.net.URI;
import java.util.function.Consumer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebSocketService {

    private WebSocketClient client;

    private final Consumer<String> onMessage;

    public WebSocketService(
            String host,
            int port,
            String playerName,
            Consumer<String> onMessage) {

        this.onMessage = onMessage;

        try {

            URI uri = new URI(
                    "ws://" + host + ":" + port
            );

            client = new WebSocketClient(uri) {

                @Override
                public void onOpen(
                        ServerHandshake handshake) {

                    System.out.println(
                            "Conectado al servidor."
                    );

                    sendRegister(playerName);
                }

                @Override
                public void onMessage(
                        String message) {

                    onMessage.accept(message);
                }

                @Override
                public void onClose(
                        int code,
                        String reason,
                        boolean remote) {

                    System.out.println(
                            "Conexión cerrada: "
                            + reason
                    );
                }

                @Override
                public void onError(Exception ex) {

                    System.out.println(
                            "Error WebSocket: "
                            + ex.getMessage()
                    );
                }
            };

            client.connect();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void sendRegister(String name) {

        org.json.JSONObject obj =
                new org.json.JSONObject();

        obj.put("type", "register");
        obj.put("name", name);

        client.send(obj.toString());
    }

    public void sendMove(
            int row,
            int col,
            int value) {

        if (client == null || !client.isOpen()) {
            return;
        }

        org.json.JSONObject obj =
                new org.json.JSONObject();

        obj.put("type", "move");
        obj.put("row", row);
        obj.put("col", col);
        obj.put("value", value);

        client.send(obj.toString());
    }

    public void close() {

        if (client != null) {
            client.close();
        }
    }
}