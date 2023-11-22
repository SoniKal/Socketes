package TCP_recup;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

public class Usuario {
    private String nombre;
    private String direccionIP;
    private Socket socket;

    private ServerSocket serverSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public Usuario(String nombre, String direccionIP) {
        this.nombre = nombre;
        this.direccionIP = direccionIP;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccionIP() {
        return direccionIP;
    }

    public void iniciarServidor() {
        try {
            serverSocket = new ServerSocket(12345); // Puerto de escucha
            System.out.println(nombre + " está esperando a que se conecte otro usuario...");
            socket = serverSocket.accept(); // Bloquea hasta que se conecta otro usuario
            System.out.println(nombre + " se ha conectado con éxito.");
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void conectar() {
        try {
            socket = new Socket(direccionIP, 12345); // 12345 es el puerto de comunicación
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println(nombre + " se ha conectado.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void desconectar() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println(nombre + " se ha desconectado.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensaje(Mensaje mensaje) {
        if (!nombre.equals(mensaje.getDestinatario())) {
            try {
                conectar();
                if (socket != null) {
                    outputStream.writeObject(mensaje);
                    System.out.println(nombre + " ha enviado un mensaje a " + mensaje.getDestinatario() + ": " + mensaje.getTexto());
                } else {
                    System.out.println("No se pudo establecer la conexión. El socket es nulo.");
                }
            } catch (IOException e) {
                System.out.println("Error al enviar el mensaje: " + e.getMessage());
            } finally {
                desconectar();
            }
        } else {
            System.out.println("No es necesario conectarse para enviar un mensaje a uno mismo.");
        }
    }

    private static String obtenerIPInterfaz() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface current = interfaces.nextElement();
                if (current.getName().equals("enp1s0") && current.isUp() && !current.isLoopback() && !current.isVirtual()) {
                    Enumeration<InetAddress> addresses = current.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress currentAddr = addresses.nextElement();
                        if (currentAddr instanceof Inet4Address) {
                            return currentAddr.getHostAddress();
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    public static void main(String[] args) {
        String ipPublica = obtenerIPInterfaz();

        if (ipPublica != null) {
            System.out.println("La dirección IP de la interfaz es: " + ipPublica);
        } else {
            System.out.println("No se pudo obtener la dirección IP de la interfaz.");
            return;
        }

        List<Usuario> usuarios = leerUsuariosDesdeArchivo("/home/fabricio_fiesta/Labo_2023 CSTCB/tp_redes/Socketes/SocketsTCP/src/TCP_recup/Topo");

        int posicion = -1;
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getDireccionIP().equals(ipPublica)) {
                posicion = i + 1;
                break;
            }
        }

        if (posicion != -1) {
            System.out.println("Hola, soy " + usuarios.get(posicion - 1).getNombre() +
                    " y estoy en la posición " + posicion + " en la topografía.");
                    usuarios.get(posicion - 1).iniciarServidor();
        } else {
            System.out.println("No se encontró la posición en la topografía.");
        }
        Scanner scanner = new Scanner(System.in);
        while (true) {

            System.out.println("Escribe el mensaje (destinatario-mensaje):");
            String entrada = scanner.nextLine();

            String[] partes = entrada.split("-");
            if (partes.length == 2) {
                String destinatario = partes[0].trim();
                String textoMensaje = partes[1].trim();

                Mensaje mensaje = new Mensaje(textoMensaje, destinatario);
                usuarios.get(0).enviarMensaje(mensaje);
            } else {
                System.out.println("Formato incorrecto. Debe ser 'destinatario-mensaje'.");
            }
        }
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
        return "REMITENTE";
    }
}
