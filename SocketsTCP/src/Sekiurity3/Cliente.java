package Sekiurity3;

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

public class Cliente {
    private Socket socket;
    private KeyPair clienteKeyPair;
    private PublicKey servidorPublicKey;

    public static void main(String[] args) {
        Cliente cliente = new Cliente();
        cliente.iniciar();
    }

    public void iniciar() {
        try {
            socket = new Socket("172.16.255.201", 6969);
            System.out.println("Conectado al servidor.");

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            clienteKeyPair = keyPairGenerator.generateKeyPair();

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // recibir clave pública del servidor
            servidorPublicKey = (PublicKey) in.readObject();
            // enviar la clave pública del cliente al servidor
            out.writeObject(clienteKeyPair.getPublic());
            out.flush();

            Thread hiloRecibirMensajes = new Thread(() -> {
                try {
                    Mensaje mensaje;
                    while ((mensaje = (Mensaje) in.readObject()) != null){
                        System.out.println(mensaje.getMensajeEncriptado());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
            hiloRecibirMensajes.start();

            Thread hiloEnviarMensajes = new Thread(() -> {
                try {
                    Scanner scanner = new Scanner(System.in);
                    while (true) {
                        String mensajeUsuario = scanner.nextLine();
                        String hash = Hash.hashear(mensajeUsuario);

                        // encriptar mensaje con la clave pública del servidor
                        String mensajeEncriptado = EncryptWithPublic(mensajeUsuario, servidorPublicKey);

                        // firmar el mensaje y encriptar con la clave privada del cliente
                        String mensajeHasheado = EncryptWithPrivate(hash, clienteKeyPair.getPrivate());
                        Mensaje mensaje = new Mensaje(mensajeEncriptado, mensajeHasheado);
                        out.writeObject(mensaje);
                        out.flush();
                        System.out.println("llego");
                    }
                } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException |
                         InvalidKeyException | IllegalBlockSizeException |
                         BadPaddingException | InvalidKeySpecException e) {
                    e.printStackTrace();
                }
            });

            hiloEnviarMensajes.start();

        } catch (IOException | NoSuchAlgorithmException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // método para encriptar utilizando una clave pública
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








