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
import java.security.spec.InvalidKeySpecException;

public class Cliente {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("172.16.255.201", 6969);
            ObjectOutputStream escritor = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream lector = new ObjectInputStream(socket.getInputStream());
            RSA rsa = new RSA();
            RSA rsaServer = new RSA();
            Hash hash = new Hash();
            rsa.genKeyPair(512);
            rsa.saveToDiskPrivateKey("/tmp/rsa.priCliente");
            rsa.saveToDiskPublicKey("/tmp/rsa.pubCliente");
            rsa.openFromDiskPrivateKey("/tmp/rsa.priCliente");
            rsa.openFromDiskPublicKey("/tmp/rsa.pubCliente");

            Mensaje claveServer = (Mensaje) lector.readObject();
            rsaServer.setPublicKeyString(claveServer.getExtra());

            Thread hiloRecibirMensajes = new Thread(() -> {
                try {
                    Mensaje mensaje;
                    while ((mensaje = (Mensaje) lector.readObject()) != null) {
                        System.out.println(mensaje.getExtra());
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            hiloRecibirMensajes.start();
            BufferedReader lectorConsola = new BufferedReader(new InputStreamReader(System.in));

            Thread hiloEnviarMensajes = new Thread(() -> {
                try {
                    String mensajeUsuario = lectorConsola.readLine();
                    String mensajeHasheado = hash.hashear(mensajeUsuario);
                    rsa.EncryptWithPrivate(mensajeUsuario);
                    String mensajeEncriptado = rsaServer.Encrypt(mensajeUsuario);

                    while (lector.readObject() != null) {
                        escritor.writeObject(new Mensaje(mensajeEncriptado, mensajeHasheado));
                        escritor.flush();
                    }
                } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException |
                         InvalidKeyException | IllegalBlockSizeException |
                         BadPaddingException | InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
            hiloEnviarMensajes.start();

            hiloRecibirMensajes.join();
            hiloEnviarMensajes.join();

            escritor.close();
            lector.close();
            lectorConsola.close();
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
}
