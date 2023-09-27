package Sekiurity3;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Servidor {
    private static final int PUERTO = 12345;
    private static Set<PrintWriter> clientesConectados = new CopyOnWriteArraySet<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado en el puerto " + PUERTO);

            while (true) {
                Socket socketCliente = serverSocket.accept();
                System.out.println("Cliente conectado desde " + socketCliente.getInetAddress());

                // Crear un nuevo hilo para manejar al cliente
                Thread clienteThread = new Thread(new ClienteHandler(socketCliente));
                clienteThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para difundir un mensaje a todos los clientes conectados
    public static void difundirMensaje(Mensaje mensaje) {
        for (PrintWriter cliente : clientesConectados) {
            cliente.println(mensaje);
            cliente.flush();
        }
    }

    // Clase interna para manejar a un cliente
    private static class ClienteHandler implements Runnable {
        private Socket socketCliente;
        private PrintWriter out;
        private String ipCliente;

        public ClienteHandler(Socket socketCliente) {
            this.socketCliente = socketCliente;
            this.ipCliente = socketCliente.getInetAddress().getHostAddress();
        }

        public void run() {
            try {
                // Crear un PrintWriter para enviar mensajes al cliente
                out = new PrintWriter(socketCliente.getOutputStream(), true);
                clientesConectados.add(out);

                // Crear un lector de entrada para recibir mensajes del cliente
                BufferedReader in = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));

                String mensajeTexto;
                while ((mensajeTexto = in.readLine()) != null) {
                    Mensaje mensaje = new Mensaje(ipCliente, mensajeTexto);
                    System.out.println("Mensaje recibido de " + ipCliente + ": " + mensajeTexto);
                    difundirMensaje(mensaje);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    // Cerrar el socket y eliminar al cliente de la lista
                    clientesConectados.remove(out);
                    socketCliente.close();
                    Mensaje mensajeDesconexion = new Mensaje(ipCliente, "se ha desconectado");
                    difundirMensaje(mensajeDesconexion);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
