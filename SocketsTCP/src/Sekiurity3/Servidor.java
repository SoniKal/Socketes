package Sekiurity3;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Servidor {
    private static final int PUERTO = 6969;
    private static Set<ObjectOutputStream> clientesConectados = new CopyOnWriteArraySet<>();

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
        private ObjectOutputStream out;
        private String emisor;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Obtener la IP del cliente
                emisor = socket.getInetAddress().getHostAddress();
                System.out.println("Cliente conectado desde " + emisor);

                // Agregar el ObjectOutputStream del cliente a la lista
                clientesConectados.add(out);

                String linea;
                while ((linea = in.readLine()) != null) {
                    // Parsear la línea recibida como un objeto Mensaje
                    Mensaje mensaje = parsearMensaje(linea);
                    if (mensaje != null) {
                        broadcastMensaje(mensaje);
                    }
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

        // Método para enviar el mensaje a todos los clientes conectados
        private void broadcastMensaje(Mensaje mensaje) {
            for (ObjectOutputStream cliente : clientesConectados) {
                try {
                    if (cliente != out) {
                        cliente.writeObject(mensaje);
                        cliente.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Método para parsear una línea en un objeto Mensaje
        private Mensaje parsearMensaje(String linea) {
            try {
                String[] partes = linea.split(": ", 2);
                if (partes.length == 2) {
                    String emisor = partes[0];
                    String texto = partes[1];
                    return new Mensaje(emisor, texto);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}





