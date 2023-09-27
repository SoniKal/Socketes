package TCP_Firma;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Servidor {
    private ServerSocket serverSocket;
    private static List<ClienteHandler> clientes;
    private KeyPair servidorKeyPair;

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }

    public void iniciar() {
        try {
            serverSocket = new ServerSocket(6969);
            System.out.println("Servidor iniciado. Esperando conexiones...");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            servidorKeyPair = keyPairGenerator.generateKeyPair(); //genera claves pub y priv
            clientes = new ArrayList<>();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión aceptada desde la dirección IP: " + clientSocket.getInetAddress().getHostAddress());

                ClienteHandler clienteHandler = new ClienteHandler(clientSocket, servidorKeyPair);
                clientes.add(clienteHandler); //añade al nuevo cliente a lista de clientes
                clienteHandler.start();
            }

        } catch (IOException | NoSuchAlgorithmException e) {
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
        private KeyPair clienteKeyPair;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private Hash hash;

        public ClienteHandler(Socket socket, KeyPair servidorKeyPair) {
            clientSocket = socket;
            clienteKeyPair = generateKeyPair();
        }

        private KeyPair generateKeyPair() { //genero claves priv y pub
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                return keyPairGenerator.generateKeyPair();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream()); //para mandar data a cliente
                in = new ObjectInputStream(clientSocket.getInputStream()); //para recibir data de cliente
                hash = new Hash();

                out.writeObject(servidorKeyPair.getPublic()); //envia publica de server a cliente
                out.flush();

                PublicKey clientePublicKey = (PublicKey) in.readObject(); // recibir clave pública del cliente

                Mensaje mensaje;
                while ((mensaje = (Mensaje) in.readObject()) != null) { //lee mensajes recibidos
                    String mensajeEncriptado = mensaje.getMensajeEncriptado();
                    String mensajeHasheado = mensaje.getMensajeHasheado();

                    String mensajeDesencriptado = DecryptWithPrivate(mensajeEncriptado, servidorKeyPair.getPrivate()); //desencripta
                    String hashDesencriptada = DecryptWithPublic(mensajeHasheado, clientePublicKey);
                    String hasher = hash.hashear(mensajeDesencriptado);

                    if(hasher.equals(hashDesencriptada)){
                        System.out.println("Mensaje de "+clientSocket.getInetAddress()+": " + mensajeDesencriptado);
                        for (ClienteHandler cliente : clientes) {
                            try {
                                String textoAHashear = Hash.hashear(mensajeDesencriptado);
                                String textoAEncriptar = EncryptWithPublic(mensajeDesencriptado, cliente.clienteKeyPair.getPublic());
                                String extra = EncryptWithPrivate(textoAHashear, servidorKeyPair.getPrivate());
                                if(cliente != this){

                                    cliente.out.writeObject(new Mensaje(textoAEncriptar, extra, cliente.getName()));
                                    cliente.out.flush();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }

            } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException |
                     NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
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
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                    // remueve el cliente de la lista al desconectar
                    clientes.remove(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String EncryptWithPublic(String mensaje, PublicKey publicKey)
                throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
                IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException,
                UnsupportedEncodingException {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(mensaje.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }

        // método para encriptar utilizando una clave privada
        private String EncryptWithPrivate(String mensaje, PrivateKey privateKey)
                throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
                IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException,
                UnsupportedEncodingException {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] encryptedBytes = cipher.doFinal(mensaje.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }
    }

        private String DecryptWithPrivate(String mensajeEncriptado, PrivateKey privateKey) //metodo para desencriptar
                throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
                IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(mensajeEncriptado));
            return new String(decryptedBytes, "UTF-8");
        }

        private String DecryptWithPublic(String firmaEncriptada, PublicKey publicKey) //metodo para desencriptar
                throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
                IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(firmaEncriptada));
            return new String(decryptedBytes, "UTF-8");
        }
    }

