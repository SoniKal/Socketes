package ClienteServidor_Extra;

import java.io.BufferedReader; //lee datos que entran
import java.io.IOException; //excepcion de errores
import java.io.InputStreamReader; //igual que el primero
import java.io.PrintWriter; //escribe datos de salida
import java.net.ServerSocket; // establecen conexiones servidor - cliente
import java.net.Socket; // lo mismo que el de arriba
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private ServerSocket ServerSockete;
    private static List<ClienteHandler> clientes;

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }

    public void iniciar() { // crea un serversocket que espera conexiones de clientes
        try {
            ServerSockete = new ServerSocket(6969);
            System.out.println("ClienteServidor_Extra.Servidor iniciado. Esperando conexiones...");

            clientes = new ArrayList<>();

            while (true) { // loop infinito que acepta conexiones de clientes
                Socket clientSocket = ServerSockete.accept();
                System.out.println("Nueva conexión aceptada");

                ClienteHandler clienteHandler = new ClienteHandler(clientSocket); // crea un clientehandler x/cliente
                clientes.add(clienteHandler); //lo añade a la lista
                clienteHandler.start(); //empieza el hilo de comunicacion
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

    private class ClienteHandler extends Thread { // al extender de thread, cada uno se ejecuta en un hilo diferente
        private Socket clientSockete;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClienteHandler(Socket socket) {

            clientSockete = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSockete.getOutputStream(), true); // establece flujos entrada y salida
                in = new BufferedReader(new InputStreamReader(clientSockete.getInputStream()));

                out.println("Ingresa nombre de usuario:");
                username = in.readLine();
                System.out.println("Nuevo usuario: " + username);

                String mensaje;
                while ((mensaje = in.readLine()) != null) { //loop : escucha mensajes de clientes y los muestra
                    System.out.println("ClienteServidor_Extra.Mensaje recibido de " + username + ": " + mensaje);

                // difunde el mensaje que el cliente envio hacia los demas ; excepto a el mismo
                    for (ClienteHandler cliente : clientes) {
                        if (cliente != this) {
                            cliente.enviarMensaje(username + ": " + mensaje);
                        }
                    }
                }

                System.out.println("Usuario desconocido: " + username);
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