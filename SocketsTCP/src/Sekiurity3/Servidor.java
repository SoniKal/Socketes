package Sekiurity3;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Servidor {
    private static final int PUERTO = 6969;
    private static Set<PrintWriter> clientesConectados = new CopyOnWriteArraySet<>();

    public static void main(String[] args) {
        System.out.println("Servidor iniciado en el puerto " + PUERTO);
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PUERTO);

            while (true) {
                new ManejadorCliente(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ManejadorCliente extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String emisor;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Obtener la IP del cliente
                emisor = socket.getInetAddress().getHostAddress();
                System.out.println("Cliente conectado desde " + emisor);

                // Agregar el PrintWriter del cliente a la lista
                clientesConectados.add(out);

                String mensaje;
                while ((mensaje = in.readLine()) != null) {
                    broadcastMensaje(new Mensaje(emisor, mensaje));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    clientesConectados.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Cliente desconectado desde " + emisor);
            }
        }

        // Enviar el mensaje a todos los clientes conectados
        private void broadcastMensaje(Mensaje mensaje) {
            for (PrintWriter cliente : clientesConectados) {
                cliente.println(mensaje.getEmisor() + ": " + mensaje.getTexto());
            }
        }
    }
}

