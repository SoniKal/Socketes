package DimeglioWork;

import java.io.BufferedReader;//lee datos que entran
import java.io.IOException;//excepcion de errores
import java.io.InputStreamReader;
import java.io.PrintWriter;//escribe datos de salida
import java.net.Socket;

public class Cliente {
    private static final long TIEMPO_ENTRE_MENSAJES = 3000; // 3 segundos de espera para evitar spam


    // ANTES DE EJECUTAR: EL SERVIDOR DEBE ESTAR ABIERTO //

    public static void main(String[] args) {
        try {
            // establece una conexión con el servidor donde está esa IP representada abajo
            Socket socket = new Socket("172.16.255.201", 6969);
            PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader lectorConsola = new BufferedReader(new InputStreamReader(System.in));

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
                } catch (IOException | InterruptedException e) {
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
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}