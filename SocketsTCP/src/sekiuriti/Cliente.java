package sekiuriti;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.Base64;

public class Cliente {
    private static final long TIEMPO_ENTRE_MENSAJES = 3000;
    private static PublicKey servidorPublicKey;
    private static PrivateKey clientePrivateKey;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 6969);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            // Obtener la clave pública del servidor
            servidorPublicKey = (PublicKey) inputStream.readObject();

            // Generar un par de claves RSA para el cliente
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            clientePrivateKey = keyPair.getPrivate();
            PublicKey clientePublicKey = keyPair.getPublic();

            // Enviar la clave pública del cliente al servidor
            outputStream.writeObject(clientePublicKey);

            // Recibir y mostrar el nombre de usuario asignado
            String nombreUsuario = inputStream.readUTF();
            System.out.println("¡Bienvenido, " + nombreUsuario + "!");

            // Hilo que recibe mensajes del servidor
            Thread recibirMensajes = new Thread(() -> {
                try {
                    while (true) {
                        Mensaje mensaje = (Mensaje) inputStream.readObject();
                        if (mensaje.verificarFirma(servidorPublicKey)) {
                            System.out.println(mensaje.getTexto());
                        } else {
                            System.out.println("Mensaje no válido recibido.");
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            recibirMensajes.start();

            // Hilo que envía mensajes al servidor
            Thread enviarMensajes = new Thread(() -> {
                try {
                    while (true) {
                        String mensajeUsuario = consoleReader.readLine();
                        Mensaje mensaje = new Mensaje(mensajeUsuario);
                        mensaje.firmar(clientePrivateKey);
                        outputStream.writeObject(mensaje);
                        outputStream.flush();
                        Thread.sleep(TIEMPO_ENTRE_MENSAJES);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            enviarMensajes.start();

        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
