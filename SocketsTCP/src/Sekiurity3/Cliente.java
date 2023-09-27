package Sekiurity3;

import java.io.*;
import java.net.*;

public class Cliente {
    private static final String SERVIDOR_IP = "172.16.255.221"; // Cambiar a la IP del servidor si es necesario
    private static final int PUERTO = 6969;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVIDOR_IP, PUERTO);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Conectado al servidor. Escribe un mensaje o 'salir' para desconectarte.");

            String userInput;
            while ((userInput = stdin.readLine()) != null) {
                out.println(userInput);
                if (userInput.equalsIgnoreCase("salir")) {
                    break;
                }
            }

            out.close();
            in.close();
            stdin.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

