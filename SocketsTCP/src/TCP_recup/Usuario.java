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

    public void conectar() {
        try {
            socket = new Socket(direccionIP, 12345); // 12345 es el puerto de comunicación
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
                conectar();
                if (socket != null && socket.isConnected()) {
                    outputStream.writeObject(mensaje);
                    System.out.println(nombre + " ha enviado un mensaje a " + mensaje.getDestinatario() + ": " + mensaje.getTexto());
                } else {
                    System.out.println("No se pudo establecer la conexión. El socket no está disponible o no está conectado.");
                }
            } catch (IOException e) {
                System.out.println("Error al enviar el mensaje: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // No desconectamos aquí para permitir que el cliente siga enviando mensajes
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
        } else {
            System.out.println("No se encontró la posición en la topografía.");
            return;
        }

        // Inicia un thread para la escucha continua del servidor
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(12345); // 12345 es el puerto de escucha
                System.out.println("Esperando a que se conecte otro usuario...");
                Socket clienteSocket = serverSocket.accept();
                System.out.println("¡Usuario conectado!");

                // Manejar la comunicación con el usuario conectado en otro thread
                new Thread(() -> {
                    try {
                        ObjectInputStream inputStream = new ObjectInputStream(clienteSocket.getInputStream());
                        while (true) {
                            Mensaje mensajeRecibido = (Mensaje) inputStream.readObject();
                            System.out.println("Mensaje recibido de " + mensajeRecibido.getRemitente() +
                                    ": " + mensajeRecibido.getTexto());
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }).start();
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
