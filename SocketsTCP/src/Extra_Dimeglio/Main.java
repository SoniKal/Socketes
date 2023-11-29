package Extra_Dimeglio;

import java.io.*;
import java.net.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Usuario usuario = new Usuario();
        String publica = Usuario.interfazIP();
        Scanner scanner = new Scanner(System.in);

        if (publica != null) {
            System.out.println("----------------------------------------------------------");
            System.out.println("Dir. IP de Interfaz: " + publica);
        } else {
            System.err.println("[ERROR OBTENCION INTERFAZ]");
            return;
        }

        usuario.importarTXT("/home/jorge/Escritorio/Socketes/SocketsTCP/src/Extra_Dimeglio/topologia");

        int posicion = -1;
        int index = 0;
        for (Usuario u : usuario.getUsuarios()) {
            if (u.getDireccionIP().equals(publica)) {
                posicion = index + 1;
                break;
            }
            index++;
        }

        if (posicion != -1) {
            System.out.println("----------------------------------------------------------");
            System.out.println("Usuario: " + usuario.getUsuarios().get(posicion - 1).getNombreUsuario());
            System.out.println("Dir. IP: " + usuario.getUsuarios().get(posicion - 1).getDireccionIP());
            System.out.println("Puerto: " + usuario.getUsuarios().get(posicion - 1).getPuertoUsuario());
            System.out.println("Posicion: [" + posicion + "]");
            System.out.println("----------------------------------------------------------");
            System.out.println("Topologia: ");
            for (Usuario z : usuario.getUsuarios()) {
                System.out.println(z.getNombreUsuario() + ": " + z.getDireccionIP());
            }
            System.out.println("----------------------------------------------------------");
            System.out.println("[FORMATO]: DESTINO");
            System.out.println("[FORMATO]: MENSAJE");
            System.out.println("----------------------------------------------------------");
        } else {
            System.err.println("[POSICION NO ENCONTRADA]");
            return;
        }

        new Thread(() -> {  // usuario puede recibir mensajes
            try {
                ServerSocket serverSocket = new ServerSocket(12345);
                System.out.println("Esperando conexiones    .   .   .");

                while (true) {
                    Socket clienteSocket = serverSocket.accept();
                    System.out.println("Cliente conectado.");

                    new Thread(() -> {
                        try {
                            while (true) {
                                ObjectInputStream inputStream = new ObjectInputStream(clienteSocket.getInputStream());
                                Mensaje mensajeRecibido = (Mensaje) inputStream.readObject();
                                for (Usuario u : usuario.getUsuarios()) {
                                    if (u.getDireccionIP().equals(publica)) {
                                        u.recibir(mensajeRecibido);
                                    }
                                }
                            }
                        } catch (IOException | ClassNotFoundException ignored) {
                        }
                    }).start();
                }
            } catch (IOException ignored) {
            }
        }).start();

        while (true) {  // usuario puede enviar mensajes
            String usuarioDest = scanner.nextLine();
            if (usuarioDest.equalsIgnoreCase("exit")) {
                break;  // Salir del bucle al ingresar "exit"
            }
            String mensajeDest = scanner.nextLine();

            Mensaje mensaje = new Mensaje(mensajeDest, usuarioDest, usuario.getNombreUsuario());

            for (Usuario uV2 : usuario.getUsuarios()) {
                if (Objects.equals(uV2.getDireccionIP(), publica)) {
                    uV2.enviar(mensaje);
                }
            }
        }
    }
}
