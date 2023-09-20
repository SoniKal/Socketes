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

    // Resto del código sin cambios
}
