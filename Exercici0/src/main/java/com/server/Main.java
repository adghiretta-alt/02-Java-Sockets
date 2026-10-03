package com.server;

public class Main {

    public static void main(String[] args) {

        int port = 3000;

        GameServer server = new GameServer(port);

        server.start();

        System.out.println(
                "Servidor funcionando en ws://localhost:"
                        + port
        );
    }
}