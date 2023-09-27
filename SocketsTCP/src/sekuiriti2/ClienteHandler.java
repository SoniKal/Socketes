package sekuiriti2;

import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.util.Base64;

public class ClienteHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private PrivateKey clavePrivada;
    private PublicKey clavePublicaCliente;

    public ClienteHandler(Socket socket) {
        clientSocket = socket;
    }

    public void enviarMensaje(Mensaje mensaje) {
        String mensajeEncriptado = mensaje.getMensajeEncriptado();
        String firma = mensaje.getFirma();

        out.println(mensajeEncriptado);
        out.println(firma);
    }

    public Mensaje recibirMensajeSeguro() throws Exception {
        String mensajeEncriptado = in.readLine();
        String firma = in.readLine();

        if (GeneradorClaves.verificarFirma(mensajeEncriptado, firma, clavePublicaCliente)) {
            byte[] mensajeDesencriptadoBytes = desencriptarMensaje(mensajeEncriptado);
            String mensajeDesencriptado = new String(mensajeDesencriptadoBytes);
            return new Mensaje(mensajeDesencriptado, firma);
        } else {
            throw new Exception("Firma digital no válida");
        }
    }

    private byte[] desencriptarMensaje(String mensajeEncriptado) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, clavePrivada);
        byte[] mensajeEncriptadoBytes = Base64.getDecoder().decode(mensajeEncriptado);
        return cipher.doFinal(mensajeEncriptadoBytes);
    }

    public void enviarClavePublica(String clavePublica) {
        out.println(clavePublica);
    }

    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println("Ingresa nombre de usuario:");
            username = in.readLine();
            System.out.println("Nuevo usuario: " + username);

            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                System.out.println("Mensaje recibido de " + username + ": " + mensaje);

                // Difunde el mensaje que el cliente envió hacia los demás, excepto a sí mismo
                for (ClienteHandler cliente : Servidor.clientes) {
                    if (cliente != this) {
                        // Encriptar el mensaje con la clave pública del receptor y firmarlo con la clave privada del emisor
                        byte[] mensajeEncriptadoBytes = GeneradorClaves.encriptarConClavePublica(username + ": " + mensaje, cliente.clavePublicaCliente);
                        String mensajeEncriptado = Base64.getEncoder().encodeToString(mensajeEncriptadoBytes);
                        String firma = GeneradorClaves.firmarMensaje(mensajeEncriptado, clavePrivada);

                        Mensaje mensajeSeguro = new Mensaje(mensajeEncriptado, firma);
                        cliente.enviarMensaje(mensajeSeguro);
                    }
                }
            }

            System.out.println("Usuario desconocido: " + username);
            Servidor.clientes.remove(this);
            clientSocket.close();

        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
}

