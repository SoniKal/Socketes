package sekuiriti2;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import java.util.Base64;

public class Cliente {
    private static final long TIEMPO_ENTRE_MENSAJES = 3000;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("172.16.255.221", 6969);
            PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader lectorConsola = new BufferedReader(new InputStreamReader(System.in));

            String clavePublicaServidor = lector.readLine(); // Recibir clave pública del servidor
            PublicKey clavePublica = GeneradorClaves.claveDesdeString(clavePublicaServidor);

            // recibe y muestra el nombre de usuario asignado
            String nombreUsuario = lector.readLine();
            System.out.println("¡Bienvenido, " + nombreUsuario + "!");

            // hilo que recibe mensajes del servidor
            Thread hiloRecibirMensajes = new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = lector.readLine()) != null) {
                        System.out.println(mensaje);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            hiloRecibirMensajes.start();

            // hilo qye envia mensajes al servidor
            Thread hiloEnviarMensajes = new Thread(() -> {
                try {
                    String mensajeUsuario;
                    while ((mensajeUsuario = lectorConsola.readLine()) != null) {
                        escritor.println(mensajeUsuario);
                        Thread.sleep(TIEMPO_ENTRE_MENSAJES); // espera de mensajes (para que no se envien rapido)
                    }
                } catch




                (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            hiloEnviarMensajes.start();

            // espera que ambos hilos terminen antes de cerrar los recursos
            hiloRecibirMensajes.join();
            hiloEnviarMensajes.join();

            // cierra los recursos utilizados
            escritor.close();
            lector.close();
            lectorConsola.close();
            socket.close();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
