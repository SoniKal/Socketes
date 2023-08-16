import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private ServerSocket servidorSocket;
    private static List<ClienteHandler> listaClientes;

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }

    public void iniciar() {
        try {
            servidorSocket = new ServerSocket(6969);
            System.out.println("Servidor iniciado. Esperando conexiones...");

            listaClientes = new ArrayList<>();

            while (true) {
                Socket socketCliente = servidorSocket.accept();
                System.out.println("Nueva conexión aceptada");

                ClienteHandler clienteHandler = new ClienteHandler(socketCliente);
                listaClientes.add(clienteHandler);
                clienteHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (servidorSocket != null) {
                try {
                    servidorSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ClienteHandler extends Thread {
        private Socket socketCliente;
        private PrintWriter escritor;
        private BufferedReader lector;
        private String nombreUsuario;

        public ClienteHandler(Socket socket) {
            socketCliente = socket;
        }

        public void run() {
            try {
                escritor = new PrintWriter(socketCliente.getOutputStream(), true);
                lector = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));

                escritor.println("Ingresa tu nombre de usuario:");
                nombreUsuario = lector.readLine();
                System.out.println("Nuevo usuario conectado: " + nombreUsuario);

                String mensaje;
                while ((mensaje = lector.readLine()) != null) {
                    System.out.println("Mensaje recibido de " + nombreUsuario + ": " + mensaje);

                    // Difusión del mensaje a todos los clientes excepto al cliente que lo envió
                    for (ClienteHandler cliente : Servidor.this.listaClientes) {
                        if (cliente != this) {
                            cliente.enviarMensaje(nombreUsuario + ": " + mensaje);
                        }
                    }
                }

                System.out.println("Usuario desconectado: " + nombreUsuario);
                listaClientes.remove(this);
                socketCliente.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (escritor != null) {
                    escritor.close();
                }
                try {
                    if (lector != null) {
                        lector.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void enviarMensaje(String mensaje) {
            escritor.println(mensaje);
        }
    }
}

