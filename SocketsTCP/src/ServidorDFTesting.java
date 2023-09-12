import ClienteServidor_Extra.Mensaje;
import ClienteServidor_Extra.RSA;
import ClienteServidor_Extra.Hash;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

public class ServidorDFTesting {
    private ServerSocket ServerSockete;
    private static List<ObjectOutputStream> clientes;
    private static List<PublicKey>clavesPublicasClientes;
    private static RSA rsa = new RSA();


    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        ServidorDFTesting servidor = new ServidorDFTesting();
        servidor.iniciar();
        rsa.openFromDiskPrivateKey("/tmp/rsa.priServer");
        rsa.openFromDiskPublicKey("/tmp/rsa.pubServer");
    }

    public void iniciar() { // crea un serversocket que espera conexiones de clientes
        try {
            ServerSockete = new ServerSocket(6969);
            System.out.println("Servidor iniciado. Esperando conexiones...");
            clientes = new ArrayList<>();

            //---- CREO CLAVES DEL SERVIDOR ----//
            rsa.genKeyPair(512);
            String file_private = "/tmp/rsa.priServer";
            String file_public = "/tmp/rsa.pubServer";

            rsa.saveToDiskPrivateKey("/tmp/rsa.priServer");
            rsa.saveToDiskPublicKey("/tmp/rsa.pubServer");
            //---- CREO CLAVES DEL SERVIDOR ----//


            while (true) { // loop infinito que acepta conexiones de clientes
                Socket clientSocket = ServerSockete.accept();
                System.out.println("Nueva conexión aceptada" + "| Cliente: " + clientSocket);

                ObjectInputStream entrada = new ObjectInputStream(clientSocket.getInputStream());
                PublicKey llaveCliente = (PublicKey) entrada.readObject();


                if (verificarClavePublica(llaveCliente) == true) {
                    clavesPublicasClientes.add(llaveCliente);

                    Thread clienteHandler = new Thread(new ClienteHandler(clientSocket, llaveCliente)); // crea un clientehandler x/cliente
                    clienteHandler.start(); //empieza el hilo de comunicacion
                } else {
                    // si la llave no está, se corta la conexion
                    clientSocket.close();
                    System.out.println("Clave publica no valida. Cliente desconectado");
                }

            }
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | ClassNotFoundException e ) {
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

    private boolean verificarClavePublica(PublicKey llaveCliente) {
        return clavesPublicasClientes.contains(llaveCliente);
    }


    private static class ClienteHandler extends Thread implements Runnable{ // al extender de thread, cada uno se ejecuta en un hilo diferente
        private Socket clientSockete;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private ObjectOutputStream salida;
        private ObjectInputStream entrada;

        public ClienteHandler(Socket socket, PublicKey publicKey) {
            clientSockete = socket;
            clientes.add(salida);
        }

        @Override
        public void run() {
            salida = null;
            try {
                salida = new ObjectOutputStream(clientSockete.getOutputStream());
                out = new PrintWriter(clientSockete.getOutputStream(), true); // establece flujos entrada y salida
                in = new BufferedReader(new InputStreamReader(clientSockete.getInputStream()));
                Mensaje nuevoMensaje = new Mensaje();

                out.println("Ingresa nombre de usuario:");
                username = in.readLine();
                System.out.println("Nuevo usuario: " + username);

                while (true) { //loop : escucha mensajes de clientes y los muestra
                    entrada = new ObjectInputStream(clientSockete.getInputStream());
                    Mensaje mensaje = (Mensaje) entrada.readObject();

                    if(rsa.Decrypt(mensaje.getMensajeEncriptado()) == rsa.DecryptWithPublic(mensaje.getMensajeHasheado())){
                        // difunde el mensaje que el cliente envio hacia los demas ; excepto a el mismo
                        for (ObjectOutputStream cliente : clientes) {
                            if (cliente != salida) {
                                cliente.writeObject(nuevoMensaje);
                                cliente.flush();
                            }
                        }
                    } else {
                        System.out.println("Brecha de seguridad");
                        clientSockete.close();
                    }
                }

            } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } finally {
                try {
                    salida.close();
                    clientSockete.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}