import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private ServerSocket ServerSockete;
    private static List<ClienteHandler> clientes;

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }

    public void iniciar() {
        try {
            ServerSockete = new ServerSocket(6969);
            System.out.println("Servidor iniciado. Esperando conexiones...");

            clientes = new ArrayList<>();

            while (true) {
                Socket clientSocket = ServerSockete.accept();
                System.out.println("Nueva conexión aceptada");

                ClienteHandler clienteHandler = new ClienteHandler(clientSocket);
                clientes.add(clienteHandler);
                clienteHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ServerSockete != null) {
                try {
                    ServerSockete.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ClienteHandler extends Thread {
        private Socket clientSockete;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClienteHandler(Socket socket) {
            clientSockete = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSockete.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSockete.getInputStream()));

                out.println("Ingresa nombre de usuario:");
                username = in.readLine();
                System.out.println("Nuevo usuario: " + username);

                String mensaje;
                while ((mensaje = in.readLine()) != null) {
                    System.out.println("Mensaje recibido de " + username + ": " + mensaje);

// Difusión del mensaje a todos los clientes excepto al cliente que lo envió
                    for (ClienteHandler cliente : Servidor.this.clientes) {
                        if (cliente != this) {
                            cliente.enviarMensaje(username + ": " + mensaje);
                        }
                    }
                }

                System.out.println("usuario desconectado: " + username);
                clientes.remove(this);
                clientSockete.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    out.close();
                }
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void enviarMensaje(String mensaje) {
            out.println(mensaje);
        }
    }
}