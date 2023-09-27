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
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Conectado al servidor. Escribe un mensaje o 'salir' para desconectarte.");

            // Hilo para recibir y mostrar mensajes del servidor
            Thread recibirMensajes = new Thread(() -> {
                try {
                    Mensaje mensajeRecibido;
                    while ((mensajeRecibido = (Mensaje) in.readObject()) != null) {
                        // Mostrar el mensaje en la consola utilizando el método toString de la clase Mensaje
                        System.out.println(mensajeRecibido);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            recibirMensajes.start();

            // Hilo principal para enviar mensajes al servidor
            String userInput;
            while ((userInput = stdin.readLine()) != null) {
                // Crear un objeto Mensaje con la IP del cliente y el texto ingresado
                Mensaje mensaje = new Mensaje(socket.getLocalAddress().getHostAddress(), userInput);
                out.writeObject(mensaje);
                out.flush();

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





