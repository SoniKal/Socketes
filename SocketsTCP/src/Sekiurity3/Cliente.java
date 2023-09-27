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

            // Obtener la IP del cliente
            final String emisor = socket.getLocalAddress().getHostAddress();

            // Hilo para recibir y mostrar mensajes del servidor
            Thread recibirMensajes = new Thread(() -> {
                try {
                    String mensajeRecibido;
                    while ((mensajeRecibido = in.readLine()) != null) {
                        System.out.println(mensajeRecibido);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            recibirMensajes.start();

            // Hilo principal para enviar mensajes al servidor
            String userInput;
            while ((userInput = stdin.readLine()) != null) {
                out.println(userInput);
                if (userInput.equalsIgnoreCase("salir")) {
                    recibirMensajes.interrupt(); // Detener el hilo de recepción antes de salir
                    break;
                }
            }

            out.close();
            stdin.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}







