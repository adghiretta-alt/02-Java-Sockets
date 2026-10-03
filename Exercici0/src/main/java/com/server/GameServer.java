package com.server;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

public class GameServer extends WebSocketServer {

    private final Map<WebSocket, Player> players = new ConcurrentHashMap<>();

    /*
     * Guardamos las casillas que ya ha acertado algún jugador.
     * Ejemplo: "3-5" significa fila 3, columna 5.
     */
    private final Set<String> solvedCells =
            ConcurrentHashMap.newKeySet();

    /*
     * Sudoku inicial.
     * 0 significa casilla vacía.
     */
    private final int[][] puzzle = {

        {5, 3, 0, 0, 7, 0, 0, 0, 0},
        {6, 0, 0, 1, 9, 5, 0, 0, 0},
        {0, 9, 8, 0, 0, 0, 0, 6, 0},

        {8, 0, 0, 0, 6, 0, 0, 0, 3},
        {4, 0, 0, 8, 0, 3, 0, 0, 1},
        {7, 0, 0, 0, 2, 0, 0, 0, 6},

        {0, 6, 0, 0, 0, 0, 2, 8, 0},
        {0, 0, 0, 4, 1, 9, 0, 0, 5},
        {0, 0, 0, 0, 8, 0, 0, 7, 9}
    };

    /*
     * Solución del Sudoku.
     */
    private final int[][] solution = {

        {5, 3, 4, 6, 7, 8, 9, 1, 2},
        {6, 7, 2, 1, 9, 5, 3, 4, 8},
        {1, 9, 8, 3, 4, 2, 5, 6, 7},

        {8, 5, 9, 7, 6, 1, 4, 2, 3},
        {4, 2, 6, 8, 5, 3, 7, 9, 1},
        {7, 1, 3, 9, 2, 4, 8, 5, 6},

        {9, 6, 1, 5, 3, 7, 2, 8, 4},
        {2, 8, 7, 4, 1, 9, 6, 3, 5},
        {3, 4, 5, 2, 8, 6, 1, 7, 9}
    };

    public GameServer(int port) {
        super(new java.net.InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

        System.out.println("Cliente conectado.");
    }

    @Override
    public void onClose(
            WebSocket conn,
            int code,
            String reason,
            boolean remote) {

        Player player = players.remove(conn);

        if (player != null) {
            System.out.println(
                    "Jugador desconectado: "
                    + player.getName()
            );

            sendPlayers();
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

        try {

            JSONObject obj = new JSONObject(message);

            String type = obj.optString("type");

            switch (type) {

                case "register":
                    registerPlayer(conn, obj);
                    break;

                case "move":
                    processMove(conn, obj);
                    break;

                default:
                    sendError(
                            conn,
                            "Tipo de mensaje desconocido."
                    );
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendError(
                    conn,
                    "Mensaje JSON incorrecto."
            );
        }
    }

    private void registerPlayer(
            WebSocket conn,
            JSONObject obj) {

        String name = obj.optString("name").trim();

        if (name.isEmpty()) {

            sendError(
                    conn,
                    "El nombre no puede estar vacío."
            );

            return;
        }

        /*
         * Comprobamos que no haya otro jugador
         * con el mismo nombre.
         */
        for (Player player : players.values()) {

            if (player.getName().equalsIgnoreCase(name)) {

                sendError(
                        conn,
                        "Ese nombre ya está utilizado."
                );

                return;
            }
        }

        Player player = new Player(conn, name);

        players.put(conn, player);

        System.out.println(
                "Jugador registrado: " + name
        );

        JSONObject response = new JSONObject();

        response.put("type", "registered");
        response.put("name", name);

        conn.send(response.toString());

        sendPlayers();

        sendBoard(conn);
    }

    private void processMove(
            WebSocket conn,
            JSONObject obj) {

        Player player = players.get(conn);

        if (player == null) {
            sendError(conn, "Jugador no registrado.");
            return;
        }

        int row = obj.optInt("row", -1);
        int col = obj.optInt("col", -1);
        int value = obj.optInt("value", -1);

        if (row < 0 || row > 8 ||
            col < 0 || col > 8 ||
            value < 1 || value > 9) {

            sendError(
                    conn,
                    "Movimiento incorrecto."
            );

            return;
        }

        /*
         * Si esa casilla ya estaba en el Sudoku original,
         * no se puede modificar.
         */
        if (puzzle[row][col] != 0) {

            sendError(
                    conn,
                    "Esa casilla ya estaba rellenada."
            );

            return;
        }

        String key = row + "-" + col;

        /*
         * Si otro jugador ya la ha acertado,
         * está bloqueada.
         */
        if (solvedCells.contains(key)) {

            sendError(
                    conn,
                    "Esa casilla ya está resuelta."
            );

            return;
        }

        /*
         * Comprobamos la solución.
         */
        if (solution[row][col] == value) {

            solvedCells.add(key);

            player.addPoints(2);

            System.out.println(
                    player.getName()
                    + " ha acertado "
                    + row + "," + col
            );

            sendBoardToAll();
            sendPlayers();

        } else {

            /*
             * Respuesta incorrecta:
             * -1 punto.
             */
            player.addPoints(-1);

            JSONObject response = new JSONObject();

            response.put("type", "wrong");
            response.put("message", "Número incorrecto.");
            response.put("score", player.getScore());

            conn.send(response.toString());

            sendPlayers();
        }
    }

    private void sendPlayers() {

        List<Player> list =
                new ArrayList<>(players.values());

        /*
         * Ordenamos por puntuación descendente.
         */
        list.sort(
                Comparator.comparingInt(Player::getScore)
                        .reversed()
        );

        JSONArray array = new JSONArray();

        for (Player player : list) {

            JSONObject obj = new JSONObject();

            obj.put("name", player.getName());
            obj.put("score", player.getScore());

            array.put(obj);
        }

        JSONObject response = new JSONObject();

        response.put("type", "players");
        response.put("players", array);

        broadcast(response);
    }

    private void sendBoardToAll() {

        for (WebSocket socket : players.keySet()) {
            sendBoard(socket);
        }
    }

    private void sendBoard(WebSocket socket) {

        JSONObject response = new JSONObject();

        response.put("type", "board");

        JSONArray puzzleArray = new JSONArray();

        for (int row = 0; row < 9; row++) {

            JSONArray rowArray = new JSONArray();

            for (int col = 0; col < 9; col++) {

                rowArray.put(puzzle[row][col]);
            }

            puzzleArray.put(rowArray);
        }

        response.put("puzzle", puzzleArray);

        JSONArray solvedArray = new JSONArray();

        for (String key : solvedCells) {

            String[] parts = key.split("-");

            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);

            JSONObject cell = new JSONObject();

            cell.put("row", row);
            cell.put("col", col);
            cell.put("value", solution[row][col]);

            solvedArray.put(cell);
        }

        response.put("solved", solvedArray);

        socket.send(response.toString());
    }

    private void sendError(
            WebSocket conn,
            String message) {

        JSONObject response = new JSONObject();

        response.put("type", "error");
        response.put("message", message);

        conn.send(response.toString());
    }

    private void broadcast(JSONObject message) {

        for (WebSocket socket : players.keySet()) {

            if (socket.isOpen()) {
                socket.send(message.toString());
            }
        }
    }

    @Override
    public void onError(
            WebSocket conn,
            Exception ex) {

        ex.printStackTrace();
    }

    @Override
    public void onStart() {

        System.out.println(
                "Servidor Sudoku iniciado."
        );

        System.out.println(
                "Puerto: " + getPort()
        );
    }
}