package Extra_Dimeglio;
import java.io.*;
import java.net.*;
import java.sql.SQLOutput;
import java.util.*;
public class Usuario {
    private String nombreUsuario;
    private String puertoUsuario;
    private String direccionIP;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ArrayList<Usuario>usuarios = new ArrayList<>();
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

    public void conectado(Mensaje mensaje) {
        try {
            Usuario destino = companeroFinder(mensaje.getUsuarioDestino());
            if (destino != null) {
                System.out.println("Intentando conectar a: " + destino.getDireccionIP() + ": " + destino.getPuertoUsuario());
                socket = new Socket(destino.getDireccionIP(), Integer.parseInt(destino.getPuertoUsuario()));
                output = new ObjectOutputStream(socket.getOutputStream());
                System.out.println("[C] " + nombreUsuario);
            } else {
                System.err.println("[E] Error-Conn: Destino no encontrado");
            }
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

        int myPosicion = 0;
        int posicionDestino = 0;
        for (int i = 0; i<usuarios.size() ; i++){
            if(usuarios.get(i) == this){
                myPosicion = i;
            } else if (usuarios.get(i).getNombreUsuario().equals(destino)){
                posicionDestino = i;
            }
        }
        if(myPosicion > posicionDestino){
            userReturn = usuarios.get(myPosicion-1);
        }else if(myPosicion < posicionDestino){
            userReturn = usuarios.get(myPosicion+1);
        }
        return userReturn;
    }
    public ArrayList<Usuario> importarTXT(String rutaArchivo) {
        ArrayList<Usuario>usuariosT = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                Usuario usuarioV2 = parsearLinea(linea);
                if (usuarioV2 != null) {

                    usuariosT.add(usuarioV2);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return usuariosT;
    }

    public Usuario parsearLinea(String linea) {
        String[] partes = linea.split(":");
        if (partes.length == 3) {
            String nombre = partes[0].trim().replace("\"", "");
            String direccionIP = partes[1].trim().replace("\"", "");
            String puerto = partes[2].trim().replace("\"", "");
            Usuario returner = new Usuario(nombre, direccionIP, puerto);
            return returner;
        } else {
            System.err.println("[LINEA NO CUMPLE CON FORMATO:"+ linea+"]");
            return null;
        }
    }
    public static String interfazIP() { //en las compus de arriba es 'enp1s0' || en las de abajo 'eno2'
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
                this.enviar(mensaje, this.usuarios);
            } else {
                System.err.println("[USUARIO CERCANO NO ENCONTRADO]");
            }
        }
    }
    public void enviar(Mensaje mensaje, ArrayList<Usuario>us) {
        this.usuarios = us;

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