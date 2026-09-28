package com.client;

import java.io.*;
import java.net.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Connectar al servidor a l'adreça localhost i port 12345
            Socket socket = new Socket("localhost", 12345);

            // Crear fluxos per llegir i escriure dades
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Enviar missatge al servidor
            output.println("Hola des del client!");

            // Llegir resposta del servidor
            String respostaServidor = input.readLine();
            System.out.println("\n---------------------------------------------");
            System.out.println("Resposta del servidor: " + respostaServidor);
            System.out.println("---------------------------------------------\n");

            // Tancar connexions
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
