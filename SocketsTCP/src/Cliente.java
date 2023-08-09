import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Cliente {
    private static final long COOLDOWN = 3000; // 3 segundos de cooldown para evitar spam

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("172.16.255.221", 6969);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

// pide nombre de usuario
            String nombreUsuario = in.readLine();
            System.out.println(nombreUsuario);

// Hilo leer mensajes
            Thread recibirMensajes = new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = in.readLine()) != null) {
                        System.out.println(mensaje);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            recibirMensajes.start();

// Hilo enviar mensajes
            Thread enviarMensajes = new Thread(() -> {
                try {
                    String userInput;
                    while ((userInput = stdIn.readLine()) != null) {
                        out.println(userInput);
                        Thread.sleep(COOLDOWN); // Pausar el hilo durante el cooldown para que no spameen mensajes
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            enviarMensajes.start();

// espera a que los hilos terminen para terminar los recursos usados
            recibirMensajes.join();
            enviarMensajes.join();

            out.close();
            in.close();
            stdIn.close();
            socket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}