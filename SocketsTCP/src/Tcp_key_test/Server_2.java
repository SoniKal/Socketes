package Tcp_key_test;

import TCP_Firma.Hash;
import TCP_Firma.Mensaje;
import TCP_Firma.Servidor;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
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

public class Server_2 {
    //asimetrica
    private ServerSocket serverSocket;
    private static List<ClienteHandler> clientes;
    private KeyPair servidorKeyPair;
    //simetrica
    private SecretKey Server_key_AES;
    private String Algorithm;
    private final int KEY_SIZE = 128;
    private final int T_LEN = 128;
    private Cipher encryptionCipher;

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }


    public void iniciar() {
        try {
            serverSocket = new ServerSocket(6968);
            System.out.println("Servidor iniciado. Esperando conexiones...");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            servidorKeyPair = keyPairGenerator.generateKeyPair(); //genera claves pub y priv
            clientes = new ArrayList<>();

            Algorithm = "AES"; // Specify the algorithm (e.g., AES)
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            Server_key_AES = generator.generateKey(); //Genera la llave


            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión aceptada desde la dirección IP: " + clientSocket.getInetAddress().getHostAddress());
                Server_2.ClienteHandler clienteHandler = new Server_2.ClienteHandler(clientSocket, servidorKeyPair);
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

        public void run(){
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream()); //para mandar data a cliente
                in = new ObjectInputStream(clientSocket.getInputStream()); //para recibir data de cliente
                hash = new Hash();
                out.writeObject(servidorKeyPair.getPublic()); //envia publica de server a cliente
                out.flush();
                claveCliente = (PublicKey) in.readObject(); // recibir clave pública del cliente

                Mensaje mensaje = null;
                String clave = secretKeyToString(Server_key_AES);
                String textoAHashear = Hash.hashear(clave);
                String textoAEncriptar = EncryptWithPublic(clave, claveCliente);
                String extra = EncryptWithPrivate(textoAHashear, servidorKeyPair.getPrivate());
                out.flush();

                Mensaje_2 mensaje2;
                while((mensaje2 = (Mensaje_2) in.readObject()) != null){
                        String mensaje_desencriptado = decrypt(mensaje2.getMensaje_encriptado());
                    for (ClienteHandler cliente:clientes) {
                        if(cliente != this){
                            String mensaje_encriptado = encrypt(mensaje_desencriptado);
                            cliente.out.writeObject(new Mensaje_2(mensaje_encriptado);
                            cliente.out.flush();
                        }
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
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
            } catch (Exception e) {
                throw new RuntimeException(e);
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
    // método para encriptar y desencriptar utilizando una clave simetrica

    public String encrypt(String message) throws Exception {
        byte[] messageInBytes = message.getBytes();
        encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        encryptionCipher.init(Cipher.ENCRYPT_MODE, Server_key_AES);
        byte[] encryptedBytes = encryptionCipher.doFinal(messageInBytes);
        return encode(encryptedBytes);
    }

    public String decrypt(String encryptedMessage) throws Exception {
        byte[] messageInBytes = decode(encryptedMessage);
        Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(T_LEN, encryptionCipher.getIV());
        decryptionCipher.init(Cipher.DECRYPT_MODE, Server_key_AES, spec);
        byte[] decryptedBytes = decryptionCipher.doFinal(messageInBytes);
        return new String(decryptedBytes);
    }

    private String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }


    public static String secretKeyToString(SecretKey secretKey) {
        byte[] keyBytes = secretKey.getEncoded();
        return Base64.getEncoder().encodeToString(keyBytes);
    } //de llave a string

    public static SecretKey stringToSecretKey(String keyString, String algorithm) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyString);
            return new SecretKeySpec(keyBytes, algorithm);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    } //de string a llave




}
