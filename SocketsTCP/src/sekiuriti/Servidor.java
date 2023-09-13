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
import java.security.PublicKey;
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
        private Hash hash;

        public ClienteHandler(Socket socket) {
            clientSocket = socket;
        }

        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());
                hash = new Hash();
                rsa = new RSA();
                rsa.openFromDiskPrivateKey("/tmp/rsa.priServer");
                rsa.openFromDiskPublicKey("/tmp/rsa.pubServer");

                if(rsa.getPublicKeyString().isEmpty() || rsa.getPrivateKeyString().isEmpty()){
                    rsa.genKeyPair(512);
                    rsa.saveToDiskPrivateKey("/tmp/rsa.priServer");
                    rsa.saveToDiskPublicKey("/tmp/rsa.pubServer");
                }

                out.writeObject(rsa.getPublicKeyString());
                out.flush();

                Mensaje claveCliente = (Mensaje) in.readObject();
                rsaCliente.setPublicKeyString(claveCliente.getExtra());

                out.writeObject(new Mensaje("Ingresa nombre de usuario:"));
                out.flush();

                Mensaje nombreUsuario = (Mensaje) in.readObject();
                username = nombreUsuario.getExtra();
                System.out.println("Nuevo usuario: " + username);

                Mensaje mensaje;
                while ((mensaje = (Mensaje) in.readObject()) != null) {
                    if(rsa.Decrypt(mensaje.getMensajeEncriptado()) == hash.hashear(rsaCliente.Decrypt(mensaje.getMensajeHasheado())))
                    System.out.println("Mensaje recibido de " + username + ": " + mensaje.getExtra());

                    for (ClienteHandler cliente : Servidor.this.clientes) {
                        if (cliente != this) {
                            cliente.enviarMensaje(new Mensaje(username + ": " + mensaje.getExtra()));
                        }
                    }
                }

                System.out.println("Usuario desconocido: " + username);
                clientes.remove(this);
                clientSocket.close();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                throw new RuntimeException(e);
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeySpecException e) {
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
