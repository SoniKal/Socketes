import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import ClienteServidor_Extra.RSA;
import ClienteServidor_Extra.Mensaje;
import ClienteServidor_Extra.Hash;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class ClienteDFTesting {
    private static final long TIEMPO_ENTRE_MENSAJES = 3000; // 3 segundos de espera para evitar spam


    // ANTES DE EJECUTAR: EL SERVIDOR DEBE ESTAR ABIERTO //

    public static void main(String[] args) throws Exception {
        try {
            // establece una conexión con el servidor donde está esa IP representada abajo
            Socket socket = new Socket("172.16.255.221", 6969);
            PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader lectorConsola = new BufferedReader(new InputStreamReader(System.in));
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            PublicKey llaveServidor;


            //---- GENERO CLAVES ----//
            RSA rsa = new RSA();
            rsa.genKeyPair(512);

            String file_private = "/tmp/rsa.priCliente";
            String file_public = "/tmp/rsa.pubCliente";

            rsa.saveToDiskPrivateKey("/tmp/rsa.priCliente");
            rsa.saveToDiskPublicKey("/tmp/rsa.pubCliente");
            rsa.openFromDiskPrivateKey("/tmp/rsa.priCliente");
            rsa.openFromDiskPublicKey("/tmp/rsa.pubCliente");
            //---- GENERO CLAVES ----//


            // recibe y muestra el nombre de usuario asignado
            String nombreUsuario = lector.readLine();
            System.out.println("¡Bienvenido, " + nombreUsuario + "!");
            salida.writeObject(rsa.getPublicKeyString());
            salida.flush();

            // hilo que recibe mensajes del servidor
            Thread hiloRecibirMensajes = new Thread(() -> {
                try {
                    Mensaje mensajeRecibido = (Mensaje) entrada.readObject();
                    String mensajeDesencriptado;
                    if(rsa.Decrypt(mensajeRecibido.getMensajeEncriptado()) == rsa.DecryptWithPublic(mensajeRecibido.getMensajeHasheado())){
                        mensajeDesencriptado = rsa.Decrypt(mensajeRecibido.getMensajeEncriptado());
                        while ((mensajeDesencriptado = lector.readLine()) != null) {
                            System.out.println(mensajeDesencriptado);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
            });
            hiloRecibirMensajes.start();

            // hilo que envia mensajes al servidor
            Scanner scanner = new Scanner(System.in);
            Thread hiloEnviarMensajes = new Thread(() -> {
                try {
                    String mensajeUsuario = scanner.nextLine();
                    Hash hasher = new Hash();
                    Mensaje mensaje = new Mensaje();

                    mensaje.setMensajeHasheado(rsa.EncryptWithPrivate(hasher.hashear(mensajeUsuario)));
                    mensaje.setMensajeEncriptado(rsa.Encrypt(mensajeUsuario));
                    mensaje.setLlavePublica(rsa.PublicKey);
                    salida.writeObject(mensaje);
                    while ((mensajeUsuario = lectorConsola.readLine()) != null) {
                        salida.flush();
                        Thread.sleep(TIEMPO_ENTRE_MENSAJES); // espera de mensajes (para que no se envien rapido)
                    }
                } catch
                (IOException | InterruptedException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException | NoSuchProviderException e) {
                    e.printStackTrace();
                }
            });
            hiloEnviarMensajes.start();

            // espera que ambos hilos terminen antes de cerrar los recursos
            hiloRecibirMensajes.join();
            hiloEnviarMensajes.join();

            // cierra los recursos utilizados
            salida.close();
            escritor.close();
            lector.close();
            lectorConsola.close();
            socket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
