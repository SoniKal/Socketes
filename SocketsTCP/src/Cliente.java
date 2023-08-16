import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Cliente {
    private static final long TIEMPO_ENTRE_MENSAJES = 3000; // 3 segundos de espera para evitar spam

    public static void main(String[] args) {
        try {
            // establece una conexión con el servidor
            Socket socket = new Socket("172.16.255.221", 6969);
            PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader lectorConsola = new BufferedReader(new InputStreamReader(System.in));

            // recibir y mostrar el nombre de usuario asignado
            String nombreUsuario = lector.readLine();
            System.out.println("¡Bienvenido, " + nombreUsuario + "!");

            // hilo para recibir mensajes del servidor
            Thread hiloRecibirMensajes = new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = lector.readLine()) != null) {
                        // verifica si el mensaje es la solicitud del nombre de usuario
                        if (mensaje.equals("Ingresa tu nombre de usuario:")) {
                            System.out.println(mensaje);
                        } else {
                            System.out.println(mensaje); // Mostrar el mensaje directamente
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            hiloRecibirMensajes.start();

            // hilo para enviar mensajes al servidor
            Thread hiloEnviarMensajes = new Thread(() -> {
                try {
                    String mensajeUsuario;
                    while ((mensajeUsuario = lectorConsola.readLine()) != null) {
                        // agregar el nombre de usuario al mensaje antes de enviarlo
                        escritor.println("[" + nombreUsuario + "]: " + mensajeUsuario);
                        Thread.sleep(TIEMPO_ENTRE_MENSAJES); // Esperar para evitar enviar mensajes muy rápido
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            hiloEnviarMensajes.start();

            // esperar a que ambos hilos terminen antes de cerrar los recursos
            hiloRecibirMensajes.join();
            hiloEnviarMensajes.join();

            // cerrar los recursos utilizados
            escritor.close();
            lector.close();
            lectorConsola.close();
            socket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
