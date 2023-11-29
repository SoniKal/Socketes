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

    public void conectado(Mensaje mensaje) {
        try {
            socket = new Socket(companeroFinder(mensaje.getUsuarioDestino()).direccionIP, 24681);
            output = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("[C] "+nombreUsuario);
        } catch (ConnectException e) {
            System.err.println("[E] Error-Conn: Rechazado");
        } catch (IOException e) {
            System.err.println("[E] Error-Conn: " + e.getMessage());
        }
    }



    public void desconectado() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("[D] " + nombreUsuario);
            }
        } catch (IOException ignored) {
        }
    }

    private Usuario companeroFinder(String destino){
        Usuario userReturn = null;
        int distanciaMasCercana = Integer.MAX_VALUE;
        Usuario temporal = null;
        for (Usuario user: usuarios)
        {
            if (user.getNombreUsuario().equals(destino)){
                temporal = user;
            }
        }
        for (Usuario vecino : companeros) {
            if (!vecino.getNombreUsuario().equals(nombreUsuario)) {
                int distancia = Math.abs(usuarios.indexOf(vecino) - usuarios.indexOf(temporal));
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
            for (int i = 0; i < usuarios.size(); i++) {
                if (usuarios.get(i).getDireccionIP().equals(this.direccionIP)) {
                    if (i - 1 >= 0) {
                        companeros.add(usuarios.get(i - 1));
                    }
                    if (i + 1 < usuarios.size()) {
                        companeros.add(usuarios.get(i + 1));
                    }
                    break;
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
            System.out.println("[M] " + mensaje.getUsuarioOrigen() + ": " + mensaje.getMensaje());
        } else {
            Usuario vecinoUser = companeroFinder(mensaje.getUsuarioDestino());
            if (vecinoUser != null) {
                System.out.println(nombreUsuario + " -- [R] --> " + vecinoUser.getNombreUsuario());
                this.enviar(mensaje);
            } else {
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
                    desconectado();
                } else {
                    System.err.println("[E] Error-Socket: No disponible");
                }
            } catch (IOException e) {
                System.err.println("[E] Error-Mensaje: " + e.getMessage());
            }
        } else {
            System.err.println("[E] Error??: No te envies mensajes");
        }
    }

}