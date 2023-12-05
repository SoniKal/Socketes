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
            System.out.println("Usuario: " + usuario.getUsuarios().get(posicion-1).getNombreUsuario());
            System.out.println("Dir. IP: " + usuario.getUsuarios().get(posicion-1).getDireccionIP());
            System.out.println("Puerto: " + usuario.getUsuarios().get(posicion-1).getPuertoUsuario());
            System.out.println("Posicion: [" + posicion+"]");
            System.out.println("----------------------------------------------------------");
            System.out.println("Topologia: ");
            for (Usuario z : usuario.getUsuarios())
            {
                System.out.println(z.getNombreUsuario() + ": " + z.getDireccionIP());
            }
            for (int i = 0; i< usuario.getUsuarios().size(); i++)
                if(i+1 != usuario.getUsuarios().size() && usuario.getUsuarios().get(i+1).getDireccionIP().equals(publica) || i != 0 && usuario.getUsuarios().get(i-1).getDireccionIP().equals(publica)){
                    usuario.getCompaneros().add(usuario.getUsuarios().get(i));
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
                ServerSocket serverSocket = new ServerSocket(12345);
                System.out.println("Esperando conexiones . . .");
                while (true) {
                    Socket clienteSocket = serverSocket.accept();
                    System.out.println("Cliente conectado.");
                    ObjectInputStream inputStream = new ObjectInputStream(clienteSocket.getInputStream());
                    new Thread(() -> {
                        try {
                            while (true) {
                                Mensaje mensajeRecibido = (Mensaje) inputStream.readObject();
                                for (Usuario uv6: usuario.getUsuarios())
                                {
                                    if (uv6.getDireccionIP().equals(publica)){
                                        uv6.recibir(mensajeRecibido);
                                    }
                                }
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
                    uV2.enviar(mensaje);
                }
            }
        }
    }
}