package sekiuriti;

import java.io.*;
import java.net.*;

public class Cliente {
    private static final long TIEMPO_ENTRE_MENSAJES = 3000;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 6969);
            ObjectOutputStream escritor = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream lector = new ObjectInputStream(socket.getInputStream());
            BufferedReader lectorConsola = new BufferedReader(new InputStreamReader(System.in));

            Mensaje nombreUsuario = (Mensaje) lector.readObject();
            System.out.println("¡Bienvenido, " + nombreUsuario.getTexto() + "!");

            Thread hiloRecibirMensajes = new Thread(() -> {
                try {
                    Mensaje mensaje;
                    while ((mensaje = (Mensaje) lector.readObject()) != null) {
                        System.out.println(mensaje.getTexto());
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            hiloRecibirMensajes.start();

            Thread hiloEnviarMensajes = new Thread(() -> {
                try {
                    String mensajeUsuario;
                    while ((mensajeUsuario = lectorConsola.readLine()) != null) {
                        escritor.writeObject(new Mensaje(mensajeUsuario));
                        escritor.flush();
                        Thread.sleep(TIEMPO_ENTRE_MENSAJES);
                    }
                } catch (IOException | InterruptedException e) {
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

