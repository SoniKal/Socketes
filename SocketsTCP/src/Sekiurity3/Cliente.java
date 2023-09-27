package Sekiurity3;

import java.io.*;
import java.net.*;

public class Cliente {
    private static final String SERVIDOR_IP = "172.16.255.221"; // Cambiar a la IP del servidor si es necesario
    private static final int PUERTO = 6969;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVIDOR_IP, PUERTO);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Conectado al servidor. Escribe un mensaje o 'salir' para desconectarte.");

            // Obtener la IP del cliente
            final String emisor = socket.getLocalAddress().getHostAddress();

            // Hilo para recibir y mostrar mensajes del servidor
            Thread recibirMensajes = new Thread(() -> {
                try {
                    ObjectInputStream objectIn = new ObjectInputStream(socket.getInputStream());
                    Mensaje mensaje;
                    while ((mensaje = (Mensaje) objectIn.readObject()) != null) {
                        // Verificar si el mensaje no proviene del mismo emisor
                        if (!mensaje.getEmisor().equals(emisor)) {
                            System.out.println(mensaje.getEmisor() + ": " + mensaje.getTexto());
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            recibirMensajes.start();

            // Hilo principal para enviar mensajes al servidor
            String userInput;
            while ((userInput = stdin.readLine()) != null) {
                Mensaje mensaje = new Mensaje(emisor, userInput);
                out.writeObject(mensaje);
                out.flush();
                if (userInput.equalsIgnoreCase("salir")) {
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






