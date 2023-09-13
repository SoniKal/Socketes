package sekiuriti;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
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
        private PublicKey clientePublicKey;

        public ClienteHandler(Socket socket) {
            clientSocket = socket;
        }

        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                // Enviar clave pública del servidor al cliente
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                KeyPair keyPair = keyPairGenerator.generateKeyPair();
                PublicKey servidorPublicKey = keyPair.getPublic();
                out.writeObject(servidorPublicKey);

                out.writeUTF("Ingresa nombre de usuario:");
                out.flush();
                username = in.readUTF();
                System.out.println("Nuevo usuario: " + username);

                String mensaje;
                while ((mensaje = in.readUTF()) != null) {
                    Mensaje mensajeRecibido = (Mensaje) in.readObject();
                    if (mensajeRecibido.verificarFirma(clientePublicKey)) {
                        System.out.println("Mensaje recibido de " + username + ": " + mensajeRecibido.getTexto());

                        // Difundir el mensaje que el cliente envió hacia los demás; excepto a él mismo
                        for (ClienteHandler cliente : Servidor.this.clientes) {
                            if (cliente != this) {
                                cliente.enviarMensaje(username + ": " + mensajeRecibido.getTexto());
                            }
                        }
                    } else {
                        System.out.println("Mensaje no válido recibido de " + username);
                    }
                }

                System.out.println("Usuario desconocido: " + username);
                clientes.remove(this);
                clientSocket.close();

            } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void enviarMensaje(String mensaje) {
            try {
                out.writeObject(new Mensaje(mensaje));
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
