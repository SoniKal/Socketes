package Tcp_Simetrico;

import TCP_Firma.Hash;
import TCP_Firma.Mensaje;

import javax.crypto.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Servidor {
    private ServerSocket serverSocket;
    private static List<Tcp_Simetrico.Servidor.ClienteHandler> clientes;
    private KeyPair servidorKeyPair;

    private SecretKey SimetricaKey;

    public static void main(String[] args) {
        Tcp_Simetrico.Servidor servidor = new Tcp_Simetrico.Servidor();
        servidor.iniciar();
    }

    public void iniciar() {
        try {
            serverSocket = new ServerSocket(6968);
            System.out.println("Servidor iniciado. Esperando conexiones...");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA"); //prepara la generacion de llaves
            keyPairGenerator.initialize(2048);

            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);

            servidorKeyPair = keyPairGenerator.generateKeyPair(); //genera claves pub y priv
            SimetricaKey = keyGenerator.generateKey(); //genera clave simetrica
            clientes = new ArrayList<>();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión aceptada desde la dirección IP: " + clientSocket.getInetAddress().getHostAddress());

                Tcp_Simetrico.Servidor.ClienteHandler clienteHandler = new Tcp_Simetrico.Servidor.ClienteHandler(clientSocket, servidorKeyPair);
                clientes.add(clienteHandler); //añade al nuevo cliente a lista de clientes
                clienteHandler.start();
            }

        } catch (IOException | NoSuchAlgorithmException e) {
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
        private KeyPair clienteKeyPair;
        private PublicKey claveCliente;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private TCP_Firma.Hash hash;

        public ClienteHandler(Socket socket, KeyPair servidorKeyPair) {
            clientSocket = socket;
            clienteKeyPair = generateKeyPair();
        }

        private KeyPair generateKeyPair() { //genero claves priv y pub
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                return keyPairGenerator.generateKeyPair();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream()); //para mandar data a cliente
                in = new ObjectInputStream(clientSocket.getInputStream()); //para recibir data de cliente
                hash = new TCP_Firma.Hash();

                out.writeObject(servidorKeyPair.getPublic()); //envia publica de server a cliente
                out.flush();

                claveCliente = (PublicKey) in.readObject(); // recibir clave pública del cliente
                byte[] keyBytes = SimetricaKey.getEncoded();
                String keyString = Base64.getEncoder().encodeToString(keyBytes);
                String SimEncriptadaPub = Encrypt(keyString, claveCliente);
                String SimLlaveSim = Encrypt(Hash.hashear(keyString), servidorKeyPair.getPrivate());
                Mensaje LlaveSimetrica = new Mensaje(SimEncriptadaPub, SimLlaveSim, this.getName());
                out.writeObject(LlaveSimetrica);
                out.flush();

                String msj;
                while ((msj = (String) in.readObject()) != null) { //lee mensajes recibidos
                    msj = decryptString(msj, SimetricaKey);
                    System.out.println("Mensaje de " + clientSocket.getInetAddress() + ": " + msj);
                    for (Tcp_Simetrico.Servidor.ClienteHandler cliente : clientes) {
                        try {
                            if (cliente != this) {
                                String enviar = encryptString(msj,SimetricaKey);
                                cliente.out.writeObject(enviar);
                                cliente.out.flush();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } catch (NoSuchPaddingException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalBlockSizeException ex) {
                throw new RuntimeException(ex);
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            } catch (BadPaddingException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidKeySpecException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidKeyException ex) {
                throw new RuntimeException(ex);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                    // remueve el cliente de la lista al desconectar
                    clientes.remove(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String Encrypt(String mensaje, Key publicKey) //metodo para encriptar
                throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
                IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException,
                UnsupportedEncodingException {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(mensaje.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }

    public static String decryptString(String encryptedString, SecretKey secretKey) throws Exception {
        //inicializo el cipher con el modo decrypt y la clave secreta
        Cipher cipher = Cipher.getInstance("AES"); //uso el sistema apropiado
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        //lo transformo en bytes base64
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedString);

       //desencripto
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // convierto los bytes a string
        String decryptedString = new String(decryptedBytes);

        return decryptedString;

    }

    public static String encryptString(String inputString, SecretKey secretKey) throws Exception {

        //inicializo con el modo de encriptacion
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        //encripto
        byte[] encryptedBytes = cipher.doFinal(inputString.getBytes());

        //lo transformo denuevo a string
        String encryptedString = Base64.getEncoder().encodeToString(encryptedBytes);

        return encryptedString;
    }

}

