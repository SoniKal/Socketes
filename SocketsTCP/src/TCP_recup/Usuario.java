package TCP_recup;

import java.io.*;
import java.net.*;
import java.util.*;

public class Usuario {
    private String nombre;
    private String direccionIP;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private static List<Usuario> Usuarios = new ArrayList<>();
    private static List<Usuario> vecinos = new ArrayList<>();

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

    public void conectar(Mensaje M) {
        try {
            socket = new Socket(encontrarVecinoMasCercano(M.getDestinatario()).direccionIP, 12345);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println(nombre + " se ha conectado.");
        } catch (ConnectException e) {
            System.out.println("Error al conectar: La conexión fue rechazada. Asegúrate de que el destinatario esté ejecutando el programa y escuchando en el puerto correcto.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error al conectar: " + e.getMessage());
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
                e.printStackTrace();
            }
        } else {
            System.out.println("No es necesario conectarse para enviar un mensaje a uno mismo.");
        }
    }

    private Usuario encontrarVecinoMasCercano(String destino) {
        Usuario vecinoMasCercano = null;
        int distanciaMasCercana = Integer.MAX_VALUE;

        for (Usuario vecino : vecinos) {
            if (!vecino.getNombre().equals(nombre)) {
                int distancia = Math.abs(Usuarios.indexOf(vecino) - Usuarios.indexOf(this));
                if (distancia < distanciaMasCercana) {
                    distanciaMasCercana = distancia;
                    vecinoMasCercano = vecino;
                }
            }
        }

        return vecinoMasCercano;
    }

    public void recibirMensaje(Mensaje mensaje) {
        if (nombre.equals(mensaje.getDestinatario())) {
            System.out.println("Mensaje recibido: " + mensaje.getTexto());
        } else {
            Usuario vecinoMasCercano = encontrarVecinoMasCercano(mensaje.getDestinatario());
            if (vecinoMasCercano != null) {
                System.out.println(nombre + " reenviando mensaje a " + vecinoMasCercano.getNombre());
                vecinoMasCercano.enviarMensaje(mensaje);
            } else {
                System.out.println("No se encontró un vecino para reenviar el mensaje.");
            }
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
        List<Usuario> vecinos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":");
                if (partes.length == 2) {
                    String nombre = partes[0].trim().replace("\"", "");
                    String direccionIP = partes[1].trim().replace("\"", "");
                    vecinos.add(new Usuario(nombre, direccionIP));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vecinos;
    }


    public static void main(String[] args) {
        String ipPublica = obtenerIPInterfaz();

        if (ipPublica != null) {
            System.out.println("La dirección IP de la interfaz es: " + ipPublica);
        } else {
            System.out.println("No se pudo obtener la dirección IP de la interfaz.");
            return;
        }

        Usuarios = leerUsuariosDesdeArchivo("/home/fabricio_fiesta/Labo_2023 CSTCB/tp_redes/Socketes/SocketsTCP/src/TCP_recup/Topo"); // Ruta real del archivo de usuarios

        int posicion = -1;
        for (int i = 0; i < Usuarios.size(); i++) {
            if (Usuarios.get(i).getDireccionIP().equals(ipPublica)) {
                posicion = i + 1;
                break;
            }
        }


        if (posicion != -1) {
            System.out.println("Hola, soy " + Usuarios.get(posicion - 1).getNombre() +
                    " y estoy en la posición " + posicion + " en la topografía.");
        } else {
            System.out.println("No se encontró la posición en la topografía.");
            return;
        }

        // Inicia un thread para la escucha continua del servidor
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(12345);
                System.out.println("Esperando a que se conecten otros usuarios...");

                while (Usuarios.size() < 2) {
                    Socket clienteSocket = serverSocket.accept();
                    System.out.println("¡Usuario conectado!");

                    ObjectInputStream inputStream = new ObjectInputStream(clienteSocket.getInputStream());

                    // Manejar la comunicación con el usuario conectado en otro thread
                    new Thread(() -> {
                        try {
                            while (true) {
                                Mensaje mensajeRecibido = (Mensaje) inputStream.readObject();
                                for (Usuario i: Usuarios
                                     ) {
                                    if (i.direccionIP.equals(ipPublica)){
                                        i.recibirMensaje(mensajeRecibido);
                                    }
                                }
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }

                // Después de que se conectan al menos dos usuarios, se muestra la topografía
                for (int i = 0; i< Usuarios.size()-1; i++)
                    if(Usuarios.get(i+1).direccionIP.equals(ipPublica) && i != 3 || i != 0 && Usuarios.get(i-1).direccionIP.equals(ipPublica)){
                       vecinos.add(Usuarios.get(i));
                    }

                System.out.println("Topografía de la red:");
                for (Usuario usuario : Usuarios) {
                    System.out.println(usuario.getNombre() + " - " + usuario.getDireccionIP());
                }

                System.out.println("vecinos: ");
                for (Usuario usuario : vecinos) {
                    System.out.println(usuario.getNombre() + " - " + usuario.getDireccionIP());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // El usuario principal puede enviar mensajes a otros usuarios
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Escribe el mensaje (destinatario-mensaje):");
            String entrada = scanner.nextLine();

            String[] partes = entrada.split("-");
            if (partes.length == 2) {
                String destinatario = partes[0].trim();
                String textoMensaje = partes[1].trim();

                Mensaje mensaje = new Mensaje(textoMensaje, destinatario);

                for (Usuario i: Usuarios
                     ) {
                    if (Objects.equals(i.direccionIP, ipPublica)){
                        i.enviarMensaje(mensaje);
                    }
                }
                break;
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

}
