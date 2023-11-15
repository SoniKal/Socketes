package TCP_recup;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Usuario {
    private String nombre;
    private String ip;
    private Usuario usuarioSiguiente;

    public Usuario(String nombre, String ip) {
        this.nombre = nombre;
        this.ip = ip;
    }

    public String getNombre() {
        return nombre;
    }

    public String getIp() {
        return ip;
    }

    public void setUsuarioSiguiente(Usuario usuarioSiguiente) {
        this.usuarioSiguiente = usuarioSiguiente;
    }

    public void imprimirVecinos() {
        System.out.println("Vecinos de " + nombre + ":");
        if (usuarioSiguiente != null) {
            System.out.println("  Nombre: " + usuarioSiguiente.nombre + ", IP: " + usuarioSiguiente.ip);
        }
    }

    public void enviarMensaje(String mensaje, String destinoIp) {
        Mensaje nuevoMensaje = new Mensaje(mensaje, destinoIp);

        try (Socket socket = new Socket(destinoIp, 12345);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject(nuevoMensaje);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recibirMensaje(Mensaje mensaje) {
        if (ip.equals(mensaje.getDestino())) {
            System.out.println("Mensaje recibido para " + nombre + ": " + mensaje.getTexto());
        } else {
            System.out.println("Reenviando mensaje...");
            enviarMensajeAlSiguiente(mensaje);
        }
    }

    private void enviarMensajeAlSiguiente(Mensaje mensaje) {
        if (usuarioSiguiente != null) {
            usuarioSiguiente.enviarMensaje(mensaje.getTexto(), mensaje.getDestino());
        }
    }

    public void iniciar() {
        imprimirVecinos();
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Esperando conexiones en " + ip);

            while (true) {
                Socket socket = serverSocket.accept();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                Mensaje mensaje = (Mensaje) in.readObject();
                recibirMensaje(mensaje);

                in.close();
                socket.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void escribirMensajePersonalizado() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Ingrese el nombre del destinatario: ");
        String nombreDestinatario = scanner.nextLine();

        System.out.print("Ingrese el mensaje: ");
        String mensaje = scanner.nextLine();

        String ipDestinatario = obtenerIpPorNombre(nombreDestinatario);
        if (ipDestinatario != null) {
            enviarMensaje(mensaje, ipDestinatario);
        } else {
            System.out.println("El destinatario no existe en la topografía.");
        }
    }

    private String obtenerIpPorNombre(String nombre) {
        for (Usuario usuario : Usuario.listaDeUsuarios) {
            if (usuario.getNombre().equals(nombre)) {
                return usuario.getIp();
            }
        }
        return null;
    }

    public static ArrayList<Usuario> listaDeUsuarios = new ArrayList<>();

    public static void main(String[] args) {
        leerTopografia();
        establecerVecinos();

        Usuario usuarioActual = listaDeUsuarios.get(0);

        new Thread(() -> usuarioActual.iniciar()).start();
        new Thread(() -> usuarioActual.escribirMensajePersonalizado()).start();
    }

    private static void leerTopografia() {
        try (BufferedReader br = new BufferedReader(new FileReader("/home/fabricio_fiesta/Labo_2023 CSTCB/tp_redes/Socketes/SocketsTCP/src/TCP_recup/Topo"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":");
                listaDeUsuarios.add(new Usuario(partes[0], partes[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void establecerVecinos() {
        int totalUsuarios = Usuario.listaDeUsuarios.size();

        for (int i = 0; i < totalUsuarios - 1; i++) {
            Usuario actual = Usuario.listaDeUsuarios.get(i);
            Usuario siguiente = Usuario.listaDeUsuarios.get(i + 1);
            actual.setUsuarioSiguiente(siguiente);
        }
    }
}

