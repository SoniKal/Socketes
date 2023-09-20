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
            rsa.genKeyPair(512);

            Mensaje claveServer = (Mensaje) lector.readObject(); // recibe publica servidor
            rsaServer.setPublicKeyString(claveServer.getExtra());

            escritor.writeObject(new Mensaje(rsa.getPublicKeyString())); //cliente envia su publica
            escritor.flush();

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

    public RSA getRsa() {
        return rsa;
    }

    public void setRsa(RSA rsa) {
        this.rsa = rsa;
    }

    public RSA getRsaServer() {
        return rsaServer;
    }

    public void setRsaServer(RSA rsaServer) {
        this.rsaServer = rsaServer;
    }

    public Hash getHash() {
        return hash;
    }

    public void setHash(Hash hash) {
        this.hash = hash;
    }
}
