import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private ServerSocket serverSocket;
    private static List<ClienteHandler> clientes;

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }

    public void iniciar() {
        try {
            serverSocket = new ServerSocket(6969);
            System.out.println("Servidor iniciado. Esperando conexiones...");

            clientes = new ArrayList<>();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión aceptada");

                ClienteHandler clienteHandler = new ClienteHandler(clientSocket);
                clientes.add(clienteHandler);
                clienteHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ClienteHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClienteHandler(Socket socket) {
            clientSocket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String mensaje;
                while ((mensaje = in.readLine()) != null) {
                    System.out.println("Mensaje recibido: " + mensaje);

// Difusión del mensaje a todos los clientes excepto al cliente que lo envió
                    for (ClienteHandler cliente : Servidor.this.clientes) {
                        if (cliente != this) {
                            cliente.enviarMensaje(mensaje);
                        }
                    }
                }

                System.out.println("Cliente desconectado");
                clientes.remove(this);
                clientSocket.close();
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