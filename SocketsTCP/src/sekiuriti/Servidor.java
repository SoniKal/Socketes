package sekiuriti;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Servidor {
    private ServerSocket serverSocket;
    private static List<ClienteHandler> clientes;
    private KeyPair serverKeyPair;

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }

    public void iniciar() {
        try {
            serverSocket = new ServerSocket(6969);
            System.out.println("Servidor iniciado. Esperando conexiones...");

            clientes = new ArrayList<>();
            serverKeyPair = generarParClaves();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión aceptada");

                ClienteHandler clienteHandler = new ClienteHandler(clientSocket);
                clientes.add(clienteHandler);
                clienteHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private KeyPair generarParClaves() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class ClienteHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private PublicKey clientPublicKey;

        public ClienteHandler(Socket socket) {
            clientSocket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Recibe la clave pública del cliente
                String publicKeyBase64 = in.readLine();
                clientPublicKey = Utilidades.convertirClavePublicaDesdeBase64(publicKeyBase64);

                out.println("Ingresa nombre de usuario:");
                username = in.readLine();
                System.out.println("Nuevo usuario: " + username);

                String mensaje;
                while ((mensaje = in.readLine()) != null) {
                    System.out.println("Mensaje recibido de " + username + ": " + mensaje);

                    // Cifra el mensaje antes de enviarlo
                    Mensaje mensajeEncriptado = new Mensaje(username + ": " + mensaje, serverKeyPair.getPrivate());
                    for (ClienteHandler cliente : clientes) {
                        if (cliente != this) {
                            cliente.enviarMensaje(mensajeEncriptado);
                        }
                    }
                }

                System.out.println("Usuario desconocido: " + username);
                clientes.remove(this);
                clientSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void enviarMensaje(Mensaje mensaje) {
            // Envía el mensaje cifrado
            out.println(mensaje.getTexto());
            out.println(Base64.getEncoder().encodeToString(mensaje.getFirma()));
        }
    }
}
