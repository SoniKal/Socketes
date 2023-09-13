package sekiuriti;

import ClienteServidor_Extra.Hash;
import ClienteServidor_Extra.RSA;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
            rsa.genKeyPair(512);
            rsa.saveToDiskPrivateKey("/tmp/rsa.priServer");
            rsa.saveToDiskPublicKey("/tmp/rsa.pubServer");
            rsa.openFromDiskPrivateKey("/tmp/rsa.priServer");
            rsa.openFromDiskPublicKey("/tmp/rsa.pubServer");

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
                 InvalidKeySpecException | InvalidKeyException e) {
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
        private ObjectInputStream in;
        private Hash hash;

        public ClienteHandler(Socket socket) {
            clientSocket = socket;
            rsaCliente = new RSA(); // Inicializar rsaCliente aquí
        }

        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());
                hash = new Hash();

                out.writeObject(new Mensaje(rsa.getPublicKeyString()));
                out.flush();

                Mensaje claveCliente = (Mensaje) in.readObject();
                rsaCliente.setPublicKeyString(claveCliente.getExtra());

                Mensaje mensaje;
                while ((mensaje = (Mensaje) in.readObject()) != null) {
                    if (rsa.Decrypt(mensaje.getMensajeEncriptado()) == hash.hashear(rsaCliente.Decrypt(mensaje.getMensajeHasheado())))
                        System.out.println("Mensaje recibido de un cliente.");

                    for (ClienteHandler cliente : Servidor.this.clientes) {
                        if (cliente != this) {
                            cliente.enviarMensaje(new Mensaje("Mensaje del servidor."));
                        }
                    }
                }

                clientes.remove(this);
                clientSocket.close();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException | IllegalBlockSizeException |
                     NoSuchAlgorithmException | BadPaddingException |
                     InvalidKeySpecException | InvalidKeyException e) {
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
