package TCP_recup;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Usuario {
    private String nombre;
    private String direccionIP;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public Usuario(String nombre, String direccionIP) {
        this.nombre = nombre;
        this.direccionIP = direccionIP;

        // Cada usuario actúa como servidor en un hilo separado
        new Thread(this::iniciarServidor).start();

        // Establecer conexión como cliente
        try {
            socket = new Socket(direccionIP, 12345); // 12345 es el puerto de comunicación
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void iniciarServidor() {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Manejar la conexión del nuevo usuario en un hilo separado
                new Thread(() -> manejarConexion(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void manejarConexion(Socket clientSocket) {
        try (
                ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            // Manejar la conexión con el usuario
            while (true) {
                // Esperar un mensaje y mostrarlo
                Mensaje mensaje = (Mensaje) inputStream.readObject();
                System.out.println(nombre + " ha recibido un mensaje de " + mensaje.getRemitente() + ": " + mensaje.getTexto());

                // Si este usuario no es el destinatario, reenviar al vecino más cercano
                if (!nombre.equals(mensaje.getDestinatario())) {
                    enviarMensajeAlVecinoMasCercano(mensaje);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void enviarMensajeAlVecinoMasCercano(Mensaje mensaje) {
        int indiceDestinatario = -1;
        int indiceActual = -1;

        for (int i = 0; i < Topografia.usuarios.size(); i++) {
            if (Topografia.usuarios.get(i).getNombre().equals(mensaje.getDestinatario())) {
                indiceDestinatario = i;
            } else if (Topografia.usuarios.get(i).getNombre().equals(this.nombre)) {
                indiceActual = i;
            }
        }

        // Enviar mensaje al vecino más cercano al destinatario
        if (indiceDestinatario != -1 && indiceActual != -1) {
            int indiceVecino = (indiceDestinatario < indiceActual) ? indiceActual - 1 : indiceActual + 1;
            if (indiceVecino >= 0 && indiceVecino < Topografia.usuarios.size()) {
                Topografia.usuarios.get(indiceVecino).enviarMensaje(mensaje);
            }
        }
    }

    public void enviarMensaje(Mensaje mensaje) {
        // Determinar si este usuario es el destinatario
        if (nombre.equals(mensaje.getDestinatario())) {
            System.out.println("Soy " + nombre + ", este mensaje es para mí: " + mensaje.getTexto());
        } else {
            // Enviar el mensaje al vecino más cercano al destinatario
            enviarMensajeAlVecinoMasCercano(mensaje);
        }
    }

    public String getNombre() {
        return nombre;
    }
}

class Mensaje implements Serializable {
    private String texto;
    private String destinatario;

    public Mensaje(String texto, String destinatario) {
        this.texto = texto;
        this.destinatario = destinatario;
    }

    public String getTexto() {
        return texto;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getRemitente() {
        // Puedes ajustar esto según tus necesidades
        return "REMITENTE";
    }
}

class Topografia {
    public static List<Usuario> usuarios = new ArrayList<>();

    public static void main(String[] args) {
        // Crear instancias de Usuario leyendo el archivo de texto
        usuarios = leerUsuariosDesdeArchivo("/home/fabricio_fiesta/Labo_2023 CSTCB/tp_redes/Socketes/SocketsTCP/src/TCP_recup/Topo");

        // Enviar mensajes de prueba
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Escribe el mensaje (destinatario-mensaje):");
            String entrada = scanner.nextLine();

            // Dividir la entrada en destinatario y mensaje
            String[] partes = entrada.split("-");
            if (partes.length == 2) {
                String destinatario = partes[0].trim();
                String textoMensaje = partes[1].trim();

                Mensaje mensaje = new Mensaje(textoMensaje, destinatario);
                usuarios.get(0).enviarMensaje(mensaje); // Enviamos el mensaje desde el primer usuario para simplificar
            } else {
                System.out.println("Formato incorrecto. Debe ser 'destinatario-mensaje'.");
            }
        }
    }

    private static List<Usuario> leerUsuariosDesdeArchivo(String rutaArchivo) {
        List<Usuario> usuarios = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":");
                if (partes.length == 2) {
                    String nombre = partes[0].trim().replace("\"", "");
                    String direccionIP = partes[1].trim().replace("\"", "");
                    usuarios.add(new Usuario(nombre, direccionIP));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return usuarios;
    }
}
