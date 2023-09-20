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
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Servidor {
    private ServerSocket serverSocket;
    private static List<ClienteHandler> clientes;
    private RSA rsa;
    private RSA rsaCliente;
    private HashMap<PublicKey, ClienteHandler>clientesTotales = new HashMap<>();

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

            clientes = new ArrayList<>();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión aceptada");

                ClienteHandler clienteHandler = new ClienteHandler(clientSocket);
                clientes.add(clienteHandler);
                System.out.println(clienteHandler);
                System.out.println(clienteHandler.getId());
                System.out.println(clienteHandler.getStackTrace());
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

                out.writeObject(new Mensaje(rsa.getPublicKeyString())); //server envia su publica
                out.flush();

                Mensaje claveCliente = (Mensaje) in.readObject();
                rsaCliente.setPublicKeyString(claveCliente.getExtra()); // recibe publica servidor

                clientesTotales.put(rsaCliente.PublicKey, ClienteHandler.this);

                Mensaje mensaje;
                mensaje = (Mensaje) in.readObject();
                    String extra1, extra2, extra3;
                    extra1 = rsa.Decrypt(mensaje.getMensajeEncriptado());
                    extra2 = hash.hashear(rsaCliente.Decrypt(mensaje.getMensajeHasheado()));
                    if (extra1 == extra2){
                        System.out.println("Mensaje recibido de un cliente.");
                        extra3 = rsaCliente.Decrypt(mensaje.getMensajeHasheado());
                        for (ClienteHandler cliente : Servidor.this.clientes) {
                            if (cliente != this) {
                                cliente.enviarMensaje(extra3);
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

        public void enviarMensaje(String mensaje) {
            try {
                out.writeObject(mensaje);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
