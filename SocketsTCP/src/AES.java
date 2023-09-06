import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;

public class AES {
    public static void main(String[] args) throws Exception {
        // La clave debe tener 16, 24 o 32 bytes para AES-128, AES-192 o AES-256, respectivamente
        String clave = "MiClaveSecreta123"; // Cambia esto por tu clave secreta
        String textoOriginal = "Hola, este es mi texto original.";

        // Genera un vector de inicialización (IV)
        byte[] iv = new byte[16];
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Crea una clave secreta a partir de la clave proporcionada
        SecretKeyFactory factory = SecretKeyFactory.getInstance("AES");
        SecretKey key = new SecretKeySpec(clave.getBytes(), "AES");

        // Inicializa el cifrado
        Cipher cifrador = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cifrador.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        // Cifra el texto original
        byte[] textoCifrado = cifrador.doFinal(textoOriginal.getBytes("UTF-8"));

        // Convierte el resultado a una representación en base64
        String textoCifradoBase64 = Base64.getEncoder().encodeToString(textoCifrado);

        System.out.println("Texto cifrado: " + textoCifradoBase64);

        // Puedes descifrar el texto cifrado de la siguiente manera
        cifrador.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] textoDescifrado = cifrador.doFinal(Base64.getDecoder().decode(textoCifradoBase64));
        String textoOriginalDescifrado = new String(textoDescifrado, "UTF-8");

        System.out.println("Texto descifrado: " + textoOriginalDescifrado);
    }
}
