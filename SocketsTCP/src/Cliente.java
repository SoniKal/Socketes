import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Cliente {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("172.16.255.221", 6969);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

// Hilo para leer mensajes del servidor
            Thread recibirMensajes = new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = in.readLine()) != null) {
                        System.out.println("Mensaje recibido: " + mensaje);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            recibirMensajes.start();

// Hilo principal para enviar mensajes al servidor
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
            }

            out.close();
            in.close();
            stdIn.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}