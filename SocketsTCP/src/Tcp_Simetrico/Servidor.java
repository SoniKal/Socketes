package Tcp_Simetrico;

import TCP_Firma.Hash;
import TCP_Firma.Mensaje;

import javax.crypto.*;
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
    private static List<Tcp_Simetrico.Servidor.ClienteHandler> clientes;
    private KeyPair servidorKeyPair;

    private SecretKey SimetricaKey;

    public static void main(String[] args) {
        Tcp_Simetrico.Servidor servidor = new Tcp_Simetrico.Servidor();
        servidor.iniciar();
    }

    public void iniciar() {
        try {
            serverSocket = new ServerSocket(6968);
            System.out.println("Servidor iniciado. Esperando conexiones...");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            servidorKeyPair = keyPairGenerator.generateKeyPair(); //genera claves pub y priv
            SimetricaKey = keyGenerator.generateKey(); //genera clave simetrica
            clientes = new ArrayList<>();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión aceptada desde la dirección IP: " + clientSocket.getInetAddress().getHostAddress());

                Tcp_Simetrico.Servidor.ClienteHandler clienteHandler = new Tcp_Simetrico.Servidor.ClienteHandler(clientSocket, servidorKeyPair);
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
        private PublicKey claveCliente;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private TCP_Firma.Hash hash;

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
                hash = new TCP_Firma.Hash();

                out.writeObject(servidorKeyPair.getPublic()); //envia publica de server a cliente
                out.flush();

                claveCliente = (PublicKey) in.readObject(); // recibir clave pública del cliente

                String SimEncriptadaPub = EncryptWithPrivate(SimetricaKey.toString(), servidorKeyPair.getPrivate());
                String SimLlaveSim = EncryptWithPublic(Hash.hashear(SimetricaKey.toString()), claveCliente);
                Mensaje LlaveSimetrica = new Mensaje(SimEncriptadaPub, SimLlaveSim, this.getName());
                out.writeObject(LlaveSimetrica);
                out.flush();

                String msj;
                while ((msj = (String) in.readObject()) != null) { //lee mensajes recibidos
                    msj = decryptString(msj, SimetricaKey);
                    System.out.println("Mensaje de " + clientSocket.getInetAddress() + ": " + msj);
                    for (Tcp_Simetrico.Servidor.ClienteHandler cliente : clientes) {
                        try {
                            if (cliente != this) {
                                cliente.out.writeObject(msj);
                                cliente.out.flush();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } catch (NoSuchPaddingException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalBlockSizeException ex) {
                throw new RuntimeException(ex);
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            } catch (BadPaddingException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidKeySpecException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidKeyException ex) {
                throw new RuntimeException(ex);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
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

    public static String decryptString(String encryptedString, SecretKey secretKey) throws Exception {
        // Initialize the Cipher with the decryption mode and the secret key
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // Use the appropriate transformation
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // Decode the Base64-encoded encrypted string to bytes
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedString);

        // Perform decryption
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // Convert the decrypted bytes to a string
        String decryptedString = new String(decryptedBytes);

        return decryptedString;

    }

    public static String encryptString(String inputString, SecretKey secretKey) throws Exception {
        // Initialize the Cipher with the encryption mode and the secret key
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // Use the appropriate transformation
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // Perform encryption
        byte[] encryptedBytes = cipher.doFinal(inputString.getBytes());

        // Encode the encrypted bytes to Base64
        String encryptedString = Base64.getEncoder().encodeToString(encryptedBytes);

        return encryptedString;
    }

}

