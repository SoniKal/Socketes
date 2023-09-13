package sekiuriti;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.Base64;

public class Cliente {
    private static final long TIEMPO_ENTRE_MENSAJES = 3000;

    public static void main(String[] args) {
        try {
            // Establece una conexión con el servidor donde está esa IP representada abajo
            Socket socket = new Socket("172.16.255.201", 6969);
            PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader lectorConsola = new BufferedReader(new InputStreamReader(System.in));

            // Genera un par de claves RSA para el cliente
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();

            // Envía la clave pública al servidor
            escritor.println(Base64.getEncoder().encodeToString(publicKey.getEncoded()));

            // Recibe y muestra el nombre de usuario asignado
            String nombreUsuario = lector.readLine();
            System.out.println("¡Bienvenido, " + nombreUsuario + "!");

            // Hilo que recibe mensajes del servidor
            Thread hiloRecibirMensajes = new Thread(() -> {
                try {
                    String mensajeTexto;
                    while ((mensajeTexto = lector.readLine()) != null) {
                        // Recibe el mensaje cifrado
                        String firmaBase64 = lector.readLine();
                        byte[] firmaBytes = Base64.getDecoder().decode(firmaBase64);

                        // Verifica la firma antes de mostrar el mensaje
                        if (Mensaje.verificarFirma(mensajeTexto, firmaBytes, publicKey)) {
                            System.out.println(mensajeTexto);
                        } else {
                            System.out.println("Mensaje no autenticado.");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            hiloRecibirMensajes.start();

            // Hilo que envía mensajes al servidor
            Thread hiloEnviarMensajes = new Thread(() -> {
                try {
                    String mensajeUsuario;
                    while ((mensajeUsuario = lectorConsola.readLine()) != null) {
                        // Cifra el mensaje antes de enviarlo
                        Mensaje mensajeEncriptado = new Mensaje(mensajeUsuario, keyPair.getPrivate());
                        escritor.println(mensajeEncriptado.getTexto());
                        escritor.println(Base64.getEncoder().encodeToString(mensajeEncriptado.getFirma()));
                        Thread.sleep(TIEMPO_ENTRE_MENSAJES);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            hiloEnviarMensajes.start();

            // Espera que ambos hilos terminen antes de cerrar los recursos
            hiloRecibirMensajes.join();
            hiloEnviarMensajes.join();

            // Cierra los recursos utilizados
            escritor.close();
            lector.close();
            lectorConsola.close();
            socket.close();
        } catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}