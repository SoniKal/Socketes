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

    public void conectado(Mensaje mensaje) throws IOException {
        try {
            socket = new Socket(companeroFinder(mensaje.getUsuarioDestino()).direccionIP, 12345);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            System.out.println("[C] "+nombreUsuario+": CONECTADO");
        } catch (ConnectException e) {
            System.err.println("[E] Error: Error al conectar al usuario destino. Problema de Destino, seguramente...");
        } catch (IOException e) {
            System.err.println("[E] Error: " + e.getMessage());
        }
    }

    public void deconectado(){
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("[D] "+nombreUsuario+": DESCONECTADO");
            }
        } catch (IOException ignored) {}
    }

    public Usuario companeroFinder(String destino){
        Usuario userReturn = new Usuario();
        int distanciaMasCercana = Integer.MAX_VALUE;
        Usuario temp = null;
        for (Usuario user: usuarios)
        {
            if (user.nombreUsuario.equals(destino)){
                temp = user;
            }
        }
        for (Usuario vecino : usuarios) {
            if (!vecino.getNombreUsuario().equals(nombreUsuario)) {
                int distancia = Math.abs(usuarios.indexOf(vecino) - usuarios.indexOf(temp));
                if (distancia < distanciaMasCercana) {
                    distanciaMasCercana = distancia;
                    userReturn = vecino;
                }
            }
        }
        return userReturn;
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
            System.out.println("[M] "+ mensaje.getUsuarioOrigen()+": " + mensaje.getMensaje());
        } else {
            Usuario vecinoUser = companeroFinder(mensaje.getUsuarioDestino());
            if (vecinoUser != null){
                System.out.println(nombreUsuario + " -- [R] --> " + vecinoUser.getNombreUsuario());
                this.enviar(mensaje);
            }else{
                System.err.println("[USUARIO CERCANO NO ENCONTRADO]");
            }
        }
    }

    public void enviar(Mensaje mensaje) {
        if (!nombreUsuario.equals(mensaje.getUsuarioDestino())) {
            try {
                conectado(mensaje);
                if (socket != null && socket.isConnected()) {
                    output.writeObject(mensaje);
                    System.out.println(nombreUsuario + " -- [R] --> " + mensaje.getUsuarioDestino());
                    deconectado();
                } else {
                    System.err.println("[E] Error: Socket no disponible");
                }
            } catch (IOException e) {
                System.err.println("[E] Error Mensaje: " + e.getMessage());
            }
        } else {
            System.err.println("[E] Error??: No te envies mensajes");
        }
    }

}