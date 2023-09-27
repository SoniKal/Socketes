package sekiuriti;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private ServerSocket serverSocket;
    private static List<ClienteHandler> clientes;
    private RSA rsa;
    private RSA rsaCliente;

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }

    public void iniciar() {
        try {
            serverSocket = new ServerSocket(6969);
            System.out.println("Servidor iniciado. Esperando conexiones...");
            rsa = new RSA();
            rsa.genKeyPair(2048);
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
        } catch (NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
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
        private PrintWriter outReader;
        private ObjectInputStream in;

        private Hash hash;

        public ClienteHandler(Socket socket) {
            clientSocket = socket;
            rsaCliente = new RSA(); // Inicializar rsaCliente aquí
        }

        public void run() {
            try {
                outReader = new PrintWriter(clientSocket.getOutputStream(), true); // establece flujos entrada y salida
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());
                hash = new Hash();

                out.writeObject(new Mensaje(rsa.getPublicKeyString())); //server envía su clave pública
                out.flush();

                Mensaje claveCliente = (Mensaje) in.readObject();
                rsaCliente.setPublicKeyString(claveCliente.getExtra()); // recibe clave pública del cliente

                Mensaje mensaje;
                while ((mensaje = (Mensaje) in.readObject()) != null) {

                    String mensajeDesencriptado, mensajeHasheado, mensajeHash;

                    mensajeDesencriptado = rsaCliente.Decrypt(mensaje.getMensajeEncriptado());
                    mensajeHasheado = hash.hashear(mensajeDesencriptado);
                    mensajeHash = rsa.DecryptWithPublic(mensaje.getExtra());

                    if (mensajeHasheado.equals(mensajeHash)) {
                        System.out.println("Mensaje recibido de un cliente.");
                        for (ClienteHandler cliente : Servidor.this.clientes) {
                            if (cliente != this) {
                                cliente.enviarMensaje(mensajeDesencriptado);
                            }
                        }
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException | IllegalBlockSizeException |
                     NoSuchAlgorithmException | BadPaddingException |
                     InvalidKeySpecException | InvalidKeyException e) {
                throw new RuntimeException(e);
            } catch (NoSuchProviderException e) {
                throw new RuntimeException(e);
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

        public void enviarMensaje(String mensaje) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException, NoSuchProviderException {
            outReader.println(mensaje);
        }
    }
}
