/*package sekiuriti;

import ClienteServidor_Extra.Hash;
import ClienteServidor_Extra.Mensaje;
import ClienteServidor_Extra.RSA;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class Cliente {
    private static RSA rsa;
    private static RSA rsaServer;
    private static Hash hash;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("172.16.255.201", 6969);
            ObjectOutputStream escritor = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream lector = new ObjectInputStream(socket.getInputStream());

            rsa = new RSA();
            rsaServer = new RSA();
            hash = new Hash();
            rsa.genKeyPair(2048);

            Mensaje claveServer = (Mensaje) lector.readObject(); // recibe publica servidor
            rsaServer.setPublicKeyString(claveServer.getExtra());

            escritor.writeObject(new Mensaje(rsa.getPublicKeyString())); //cliente envia su publica
            escritor.flush();

            Thread hiloRecibirMensajes = new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = lector.readLine()) != null){
                        System.out.println(mensaje);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            hiloRecibirMensajes.start();

            Thread hiloEnviarMensajes = new Thread(() -> {
                try {
                    Scanner scanner = new Scanner(System.in);
                    String mensajeUsuario = scanner.nextLine();
                    String mensajeHasheado = hash.hashear(mensajeUsuario);
                    rsa.EncryptWithPrivate(mensajeHasheado);
                    String mensajeEncriptado = rsaServer.Encrypt(mensajeUsuario);
                    escritor.writeObject(new Mensaje(mensajeEncriptado, mensajeHasheado));
                    escritor.flush();

                } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException |
                         InvalidKeyException | IllegalBlockSizeException |
                         BadPaddingException | InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    throw new RuntimeException(e);
                }
            });
            hiloEnviarMensajes.start();

            hiloRecibirMensajes.join();
            hiloEnviarMensajes.join();

            escritor.close();
            lector.close();
            socket.close();
        } catch (IOException | InterruptedException | ClassNotFoundException | NoSuchAlgorithmException |
                 InvalidKeySpecException e) {
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
}*/