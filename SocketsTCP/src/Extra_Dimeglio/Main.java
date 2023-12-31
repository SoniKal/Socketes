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
        usuario.setUsuarios(usuario.importarTXT("/home/alumno/Imágenes/Socketes/SocketsTCP/src/Extra_Dimeglio/topologia"));
        int posicion = -1;
        int index = 0;
        for (Usuario u : usuario.getUsuarios()) {
            if (u.getDireccionIP().equals(publica)) {
                posicion = index + 1;
                break;
            }
            index++;
        }
        if (posicion != -1) { //muestra toda la info. de las conexiones
            System.out.println("----------------------------------------------------------");
            System.out.println("Usuario: " + usuario.getUsuarios().get(posicion-1).getNombreUsuario());
            System.out.println("Dir. IP: " + usuario.getUsuarios().get(posicion-1).getDireccionIP());
            System.out.println("Puerto: " + usuario.getUsuarios().get(posicion-1).getPuertoUsuario());
            System.out.println("Posicion: [" + posicion+"]");
            usuario.setNombreUsuario(usuario.getUsuarios().get(posicion-1).getNombreUsuario());
            usuario.setDireccionIP(usuario.getUsuarios().get(posicion-1).getDireccionIP());
            usuario.setPuertoUsuario(usuario.getUsuarios().get(posicion-1).getPuertoUsuario());
            System.out.println("----------------------------------------------------------");
            System.out.println("Topologia: ");
            for (Usuario z : usuario.getUsuarios())
            {
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
        new Thread(() -> { //usuario puede recibir mensajes
            try {
                ServerSocket serverSocket = new ServerSocket(32000);
                System.out.println("Esperando conexiones . . .");
                while (true) {
                    Socket clienteSocket = serverSocket.accept();
                    ObjectInputStream inputStream = new ObjectInputStream(clienteSocket.getInputStream());
                    new Thread(() -> {
                        try {
                            while (true) {
                                Mensaje mensajeRecibido = (Mensaje) inputStream.readObject();
                                usuario.recibir(mensajeRecibido);
                            }
                        } catch (IOException | ClassNotFoundException ignored) {}
                    }).start();
                }
            } catch (IOException ignored) {
            }
        }).start();
        while (true) { //usuario puede enviar mensajes
            String destinatario = scanner.nextLine().toUpperCase();
            String textoMensaje = scanner.nextLine().toUpperCase();
            Mensaje mensaje = new Mensaje(textoMensaje, destinatario, usuario.getNombreUsuario());
            for (Usuario uV2 : usuario.getUsuarios()) {
                if (Objects.equals(uV2.getDireccionIP(), publica)) {
                    uV2.enviar(mensaje, usuario.getUsuarios());
                }
            }
        }
    }
}