package sekiuriti;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private ServerSocket serverSocket;
    private static List<ClienteHandler> clientes;

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }

    public void iniciar() {
        try {
            serverSocket = new ServerSocket(6969);
            System.out.println("Servidor iniciado. Esperando conexiones...");

            clientes = new ArrayList<>();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión aceptada");

                ClienteHandler clienteHandler = new ClienteHandler(clientSocket);
                clientes.add(clienteHandler);
                clienteHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ClienteHandler extends Thread {
        private Socket clientSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String username;

        public ClienteHandler(Socket socket) {
            clientSocket = socket;
        }

        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                out.writeObject(new Mensaje("Ingresa nombre de usuario:"));
                out.flush();

                Mensaje nombreUsuario = (Mensaje) in.readObject();
                username = nombreUsuario.getTexto();
                System.out.println("Nuevo usuario: " + username);

                Mensaje mensaje;
                while ((mensaje = (Mensaje) in.readObject()) != null) {
                    System.out.println("Mensaje recibido de " + username + ": " + mensaje.getTexto());

                    for (ClienteHandler cliente : Servidor.this.clientes) {
                        if (cliente != this) {
                            cliente.enviarMensaje(new Mensaje(username + ": " + mensaje.getTexto()));
                        }
                    }
                }

                System.out.println("Usuario desconocido: " + username);
                clientes.remove(this);
                clientSocket.close();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void enviarMensaje(Mensaje mensaje) {
            try {
                out.writeObject(mensaje);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
