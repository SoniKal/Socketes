package Tcp_Simetrico;

import TCP_Firma.Hash;
import TCP_Firma.Mensaje;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;


public class Cliente {
    private Socket socket;
    private KeyPair clienteKeyPair;
    private PublicKey servidorPublicKey;

    private SecretKey AesKey;


    public static void main(String[] args) {
        Tcp_Simetrico.Cliente cliente = new Tcp_Simetrico.Cliente();
        cliente.iniciar();
    }

    public void iniciar() {
        try {
            socket = new Socket("172.16.255.221", 6968);
            System.out.println("Conectado al servidor.");

            //crea la clave publica y pribada
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            clienteKeyPair = keyPairGenerator.generateKeyPair();

            //lee y envia mensajes
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // recibir clave pública del servidor
            servidorPublicKey = (PublicKey) in.readObject();


            // enviar la clave pública del cliente al servidor
            out.writeObject(clienteKeyPair.getPublic());
            out.flush();
            TCP_Firma.Mensaje Aes = (TCP_Firma.Mensaje) in.readObject();
            String mensajeEncriptadoAes = Aes.getMensajeEncriptado();
            String mensajeHasheadoAes = Aes.getMensajeHasheado();
            String mensajeDesencriptadoAes = Decrypt(mensajeEncriptadoAes, clienteKeyPair.getPrivate()); //desencripta
            String hashDesencriptadaAes = Decrypt(mensajeHasheadoAes, servidorPublicKey);
            String hasherAes = Hash.hashear(mensajeDesencriptadoAes);
            byte[] decodedKey = Base64.getDecoder().decode(mensajeDesencriptadoAes.toString());
            if (hasherAes.equals(hashDesencriptadaAes)) {
                AesKey = new SecretKeySpec(decodedKey, "AES");
            }


            Thread hiloRecibirMensajes = new Thread(() -> {
                try {
                    String msj;
                    while ((msj = (String) in.readObject()) != null) { //lee mensajes recibidos
                        msj = decryptString(msj,AesKey);
                        System.out.println("Recibido: "+msj);
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
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });


            hiloRecibirMensajes.start();


            Thread hiloEnviarMensajes = new Thread(() -> {
                try {
                    Scanner scanner = new Scanner(System.in);
                    while (true) {
                        String mensajeUsuario = scanner.nextLine();

                        // encriptar mensaje con la clave pública del servidor
                        String mensajeEncriptado = encryptString(mensajeUsuario,AesKey);

                        // firmar el mensaje y encriptar con la clave privada del cliente
                        out.writeObject(mensajeEncriptado);
                        out.flush();
                    }
                } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException |
                         InvalidKeyException | IllegalBlockSizeException |
                         BadPaddingException | InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            hiloEnviarMensajes.start();

        } catch (IOException | NoSuchAlgorithmException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private String Decrypt(String mensajeEncriptado,Key privateKey)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(mensajeEncriptado));
        return new String(decryptedBytes, "UTF-8");
    }

    public static String decryptString(String encryptedString, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedString);
        
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        String decryptedString = new String(decryptedBytes);

        return decryptedString;

    }

    public static String encryptString(String inputString, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        byte[] encryptedBytes = cipher.doFinal(inputString.getBytes());
        
        String encryptedString = Base64.getEncoder().encodeToString(encryptedBytes);

        return encryptedString;
    }
}
