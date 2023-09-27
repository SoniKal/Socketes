package Sekiurity3;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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
            servidorKeyPair = keyPairGenerator.generateKeyPair();
            clientes = new ArrayList<>();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión aceptada");

                ClienteHandler clienteHandler = new ClienteHandler(clientSocket, servidorKeyPair);
                clientes.add(clienteHandler);
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
        private KeyPair clienteKeyPair; // Se agrega el par de claves para el cliente
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private Hash hash;

        public ClienteHandler(Socket socket, KeyPair ClienteKeyPair) {
            clientSocket = socket;
            clienteKeyPair = generateKeyPair(); // Generar par de claves para el cliente
            this.clienteKeyPair = ClienteKeyPair;
        }

        // Generar un par de claves RSA para el cliente
        private KeyPair generateKeyPair() {
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
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());
                hash = new Hash();

                // Enviar clave pública del servidor al cliente
                out.writeObject(servidorKeyPair.getPublic());
                out.flush();

                // Recibir y almacenar la clave pública del cliente
                PublicKey clientePublicKey = (PublicKey) in.readObject();

                Mensaje mensaje;
                while ((mensaje = (Mensaje) in.readObject()) != null) {
                    String mensajeEncriptado = mensaje.getMensajeEncriptado();
                    String firmaEncriptada = mensaje.getFirma();

                    // Desencriptar el mensaje con la clave privada del servidor
                    String mensajeDesencriptado = DecryptWithPrivate(mensajeEncriptado, servidorKeyPair.getPrivate());

                    // Desencriptar la firma con la clave pública del cliente
                    String firmaDesencriptada = DecryptWithPublic(firmaEncriptada, clientePublicKey);

                    // Calcular el hash del mensaje original
                    String mensajeHasheado = hash.hashear(mensajeDesencriptado);

                    // Verificar la firma
                    if (firmaDesencriptada.equals(mensajeHasheado)) {
                        System.out.println("Mensaje recibido de un cliente: " + mensajeDesencriptado);
                    }
                }

            } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeySpecException |
                     NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Métodos de encriptación
        // ...

        // Método para encriptar utilizando una clave pública
        private String EncryptWithPublic(String mensaje, PublicKey publicKey)
                throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
                IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException,
                UnsupportedEncodingException {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(mensaje.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }

        // Método para encriptar utilizando una clave privada
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
}








