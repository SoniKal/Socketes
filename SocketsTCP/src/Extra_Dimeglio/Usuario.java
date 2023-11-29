package Extra_Dimeglio;
import java.io.*;
import java.net.*;
import java.util.*;

public class Usuario {
    private String nombreUsuario;
    private String puertoUsuario;
    private String direccionIP;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ArrayList<Usuario>usuarios = new ArrayList<>();
    private ArrayList<Usuario>companeros = new ArrayList<>();

    public Usuario(String n, String ip, String p) {
        nombreUsuario = n;
        direccionIP = ip;
        puertoUsuario = p;
    }

    public Usuario() {
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPuertoUsuario() {
        return puertoUsuario;
    }

    public void setPuertoUsuario(String puertoUsuario) {
        this.puertoUsuario = puertoUsuario;
    }

    public String getDireccionIP() {
        return direccionIP;
    }

    public void setDireccionIP(String direccionIP) {
        this.direccionIP = direccionIP;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public ObjectOutputStream getOutput() {
        return output;
    }

    public void setOutput(ObjectOutputStream output) {
        this.output = output;
    }

    public ObjectInputStream getInput() {
        return input;
    }

    public void setInput(ObjectInputStream input) {
        this.input = input;
    }

    public ArrayList<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(ArrayList<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public ArrayList<Usuario> getCompaneros() {
        return companeros;
    }

    public void setCompaneros(ArrayList<Usuario> companeros) {
        this.companeros = companeros;
    }

    public void inicio() throws IOException {
        socket = new Socket("172.16.255.221", 6968);

    }

    public void importarTXT(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                Usuario usuario = parsearLinea(linea);
                if (usuario != null) {
                    usuarios.add(usuario);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Usuario parsearLinea(String linea) {
        String[] partes = linea.split(":");

        if (partes.length == 3) {
            String nombre = partes[0].trim().replace("\"", "");
            String direccionIP = partes[1].trim().replace("\"", "");
            String puerto = partes[2].trim().replace("\"", "");
            return new Usuario(nombre, direccionIP, puerto);
        } else {
            System.err.println("[LINEA NO CUMPLE CON FORMATO:"+ linea+"]");
            return null;
        }
    }

    public static String interfazIP() {

        try {
            Enumeration<NetworkInterface> interfaz = NetworkInterface.getNetworkInterfaces();
            while (interfaz.hasMoreElements()) {
                NetworkInterface actual = interfaz.nextElement();
                if (actual.getName().equals("enp1s0") && actual.isUp() && !actual.isLoopback() && !actual.isVirtual()) {
                    Enumeration<InetAddress> addresses = actual.getInetAddresses();
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
            return null;
        }
    }

    public void recibir(Mensaje mensaje) {
        if (nombreUsuario.equals(mensaje.getUsuarioDestino())) {
            System.out.println("Mensaje de "+ mensaje.getUsuarioOrigen()+": " + mensaje.getMensaje());
        } else {
            Usuario vecinoMasCercano = encontrarVecinoMasCercano(mensaje.getUsuarioDestino());
            if (vecinoMasCercano != null) {
                System.out.println(nombreUsuario + " reenviando mensaje a " + vecinoMasCercano.getNombre());
                this.enviar(mensaje);
            } else {
                System.out.println("No se encontró un vecino para reenviar el mensaje.");
            }
        }
    }

    public void enviar(Mensaje mensaje) {
        if (!nombre.equals(mensaje.getDestinatario())) {
            try {
                conectar(mensaje);
                if (socket != null && socket.isConnected()) {
                    outputStream.writeObject(mensaje);
                    System.out.println(nombre + " ha enviado un mensaje a " + mensaje.getDestinatario() + ": " + mensaje.getTexto());
                    desconectar(); // Desconectar después de enviar el mensaje
                } else {
                    System.out.println("No se pudo establecer la conexión. El socket no está disponible o no está conectado.");
                }
            } catch (IOException e) {
                System.out.println("Error al enviar el mensaje: " + e.getMessage());
            }
        } else {
            System.out.println("No es necesario conectarse para enviar un mensaje a uno mismo.");
        }
    }

}
