package Tcp_key_test;

import TCP_Firma.Hash;
import TCP_Firma.Mensaje;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class User_2 {
    private SecretKey cliente_key;
    private final int KEY_SIZE = 128;
    private final int T_LEN = 128;
    private String Algorithm = "AES";
    private Socket socket;
    private Cipher encryptionCipher;
    private KeyPair clienteKeyPair;
    private PublicKey servidorPublicKey;

    public static void main(String[] args) throws Exception {
        User_2 user = new User_2();
        user.init();
    }

    public void init() throws Exception {
        //se conecta al server
        socket = new Socket("172.16.255.201", 6968);
        System.out.println("Conectado al servidor.");

        //envia y recibe mensajes del socket
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        // recibir clave pública del servidor
        servidorPublicKey = (PublicKey) in.readObject();


        // enviar la clave pública del cliente al servidor
        out.writeObject(clienteKeyPair.getPublic());
        out.flush();

        try {
            Mensaje mensaje = ((Mensaje) in.readObject());
            Hash hash = new Hash();
            String mensajeEncriptado = mensaje.getMensajeEncriptado();
            String mensajeHasheado = mensaje.getMensajeHasheado();
            String mensajeDesencriptado = DecryptWithPrivate(mensajeEncriptado, clienteKeyPair.getPrivate()); //desencripta
            String hashDesencriptada = DecryptWithPublic(mensajeHasheado, servidorPublicKey);
            String hasher = hash.hashear(mensajeDesencriptado);
            if(hasher.equals(hashDesencriptada)){
               cliente_key.equals(stringToSecretKey(mensajeDesencriptado,Algorithm)); //asigna a clave pasandola de string a key
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        }
        Thread hiloRecibirMensajes = new Thread(() -> {
            try {
                Mensaje_2 mensaje2;
                while ((mensaje2 = (Mensaje_2) in.readObject()) != null){
                    String mensaje = decrypt(mensaje2.getMensaje_encriptado());
                    System.out.println(mensaje);
                }
            }catch (Exception e){}
        });

        hiloRecibirMensajes.start();

        Thread enViarMensaje = new Thread(() -> {
            try {
                Scanner scanner = new Scanner(System.in);
                while (true){
                    String mensaje_a_encriptar = scanner.next();
                    String mensaje_encriptado = encrypt(mensaje_a_encriptar);
                    Mensaje_2 mensaje = new Mensaje_2(mensaje_encriptado);
                    out.writeObject(mensaje);
                    out.flush();
                }
            }catch (Exception e){}
        });
        enViarMensaje.start();
    }


    // método para encriptar utilizando una clave pública asimetrico

    private String DecryptWithPublic(String firmaEncriptada, PublicKey publicKey) //metodo para desencriptar
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(firmaEncriptada));
        return new String(decryptedBytes, "UTF-8");
    }

    // método para encriptar utilizando una clave privada asimetrico

    private String DecryptWithPrivate(String mensajeEncriptado, PrivateKey privateKey) //metodo para desencriptar
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(mensajeEncriptado));
        return new String(decryptedBytes, "UTF-8");
    }

    // método para encriptar y desencriptar utilizando una clave simetrica

    public String encrypt(String message) throws Exception {
        byte[] messageInBytes = message.getBytes();
        encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        encryptionCipher.init(Cipher.ENCRYPT_MODE, cliente_key);
        byte[] encryptedBytes = encryptionCipher.doFinal(messageInBytes);
        return encode(encryptedBytes);
    }

    public String decrypt(String encryptedMessage) throws Exception {
        byte[] messageInBytes = decode(encryptedMessage);
        Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(T_LEN, encryptionCipher.getIV());
        decryptionCipher.init(Cipher.DECRYPT_MODE, cliente_key, spec);
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
