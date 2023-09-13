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
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class Cliente {
    private static final long TIEMPO_ENTRE_MENSAJES = 3000;

    public static void main(String[] args) throws Exception{
        try {
            Socket socket = new Socket("172.16.255.201", 6969);
            ObjectOutputStream escritor = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream lector = new ObjectInputStream(socket.getInputStream());
            BufferedReader lectorConsola = new BufferedReader(new InputStreamReader(System.in));
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

            escritor.writeObject(new Mensaje(rsa.getPublicKeyString()));
            escritor.flush();

            Mensaje nombreUsuario = (Mensaje) lector.readObject();
            System.out.println("¡Bienvenido, " + nombreUsuario.getExtra() + "!");

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
            Scanner scanner = new Scanner(System.in);

            Thread hiloEnviarMensajes = new Thread(() -> {
                try {
                    String mensajeUsuario = scanner.nextLine();
                    String mensajeHasheado = hash.hashear(mensajeUsuario);
                    rsa.EncryptWithPrivate(mensajeUsuario);
                    String mensajeEncriptado = rsaServer.Encrypt(mensajeUsuario);

                    while ((mensajeUsuario = lectorConsola.readLine()) != null) {
                        escritor.writeObject(new Mensaje(mensajeEncriptado, mensajeHasheado, nombreUsuario.getExtra()));
                        escritor.flush();
                        Thread.sleep(TIEMPO_ENTRE_MENSAJES);
                    }
                } catch (IOException | InterruptedException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException | NoSuchProviderException e) {
                    e.printStackTrace();
                }
            });
            hiloEnviarMensajes.start();

            hiloRecibirMensajes.join();
            hiloEnviarMensajes.join();

            escritor.close();
            lector.close();
            lectorConsola.close();
            socket.close();
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

