import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;//lee datos que entran
import java.io.IOException;//excepcion de errores
import java.io.InputStreamReader;
import java.io.PrintWriter;//escribe datos de salida
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class Cliente {
    private static final long TIEMPO_ENTRE_MENSAJES = 3000; // 3 segundos de espera para evitar spam
    private static RSA rsa = new RSA();
    private static RSA rsa2 = new RSA();
    private static String file_private;
    private static String file_public;

    // ANTES DE EJECUTAR: EL SERVIDOR DEBE ESTAR ABIERTO //

    public static void main(String[] args) {
        try {
            rsa.genKeyPair(512);
            file_private = "/tmp/rsa.pri";
            file_public = "/tmp/rsa.pub";
            rsa.saveToDiskPrivateKey("/tmp/rsa.pri");
            rsa.saveToDiskPublicKey("/tmp/rsa.pub");
            rsa2.openFromDiskPrivateKey("/tmp/rsa.pri");
            rsa2.openFromDiskPublicKey("/tmp/rsa.pub");
            // establece una conexión con el servidor donde está esa IP representada abajo
            Socket socket = new Socket("172.16.255.221", 6969);
            PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader lectorConsola = new BufferedReader(new InputStreamReader(System.in));

            // recibe y muestra el nombre de usuario asignado
            String nombreUsuario = lector.readLine();
            System.out.println("¡Bienvenido, " + nombreUsuario + "!");

            // hilo que recibe mensajes del servidor
            Thread hiloRecibirMensajes = new Thread(() -> {
                try {
                    String mensaje;
                    String mensajeDesencriptado = rsa2.Decrypt(mensaje);
                    while ((mensajeDesencriptado = lector.readLine()) != null) {
                        System.out.println(mensajeDesencriptado);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            hiloRecibirMensajes.start();

            // hilo que envia mensajes al servidor
            Thread hiloEnviarMensajes = new Thread(() -> {
                try {
                    String mensajeUsuario;
                    String mensajeExtra = rsa.Encrypt(mensajeUsuario);
                    Mensaje mensaje = new Mensaje(mensajeUsuario);
                    while ((mensaje.getMensajeHasheadoEncriptado() = lectorConsola.readLine()) != null) {
                        escritor.println(mensaje.getMensajeHasheadoEncriptado());
                        Thread.sleep(TIEMPO_ENTRE_MENSAJES); // espera de mensajes (para que no se envien rapido)
                    }
                } catch




                (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            hiloEnviarMensajes.start();

            // espera que ambos hilos terminen antes de cerrar los recursos
            hiloRecibirMensajes.join();
            hiloEnviarMensajes.join();

            // cierra los recursos utilizados
            escritor.close();
            lector.close();
            lectorConsola.close();
            socket.close();
        } catch (IOException | InterruptedException | NoSuchAlgorithmException | NoSuchPaddingException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static RSA getRsa() {
        return rsa;
    }

    public static void setRsa(RSA rsa) {
        Cliente.rsa = rsa;
    }
}
