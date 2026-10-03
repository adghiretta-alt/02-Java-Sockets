package com.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ConfigController {

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtHost;

    @FXML
    private TextField txtPort;

    @FXML
    private Label lblMessage;

    @FXML
    private void connect() {

        String name =
                txtName.getText().trim();

        String host =
                txtHost.getText().trim();

        String portText =
                txtPort.getText().trim();

        if (name.isEmpty()) {

            showError(
                    "Introduce un nombre."
            );

            return;
        }

        try {

            int port =
                    Integer.parseInt(portText);

            Main.instance.connect(
                    host,
                    port,
                    name
            );

            lblMessage.setText(
                    "Conectando..."
            );

        } catch (NumberFormatException e) {

            showError(
                    "El puerto debe ser un número."
            );
        }
    }

    public void showError(String message) {

        lblMessage.setText(message);
    }
}