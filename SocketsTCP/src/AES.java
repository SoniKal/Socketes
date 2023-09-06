import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;

public class AES {
    public static void main(String[] args) throws Exception {
        // clave debe tener 16, 24 o 32 bytes para AES
        String clave = "dummytest"; // Cambia esto por tu clave secreta
        String textoOriginal = "hello world";

        byte[] iv = new byte[16];
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // crea clave
        SecretKeyFactory factory = SecretKeyFactory.getInstance("AES");
        SecretKey key = new SecretKeySpec(clave.getBytes(), "AES");

        // inicia cifrado
        Cipher cifrador = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cifrador.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        // texto original cifrado
        byte[] textoCifrado = cifrador.doFinal(textoOriginal.getBytes("UTF-8"));

        // convierte el resultado a base64
        String textoCifradoBase64 = Base64.getEncoder().encodeToString(textoCifrado);

        System.out.println("Texto cifrado: " + textoCifradoBase64);

        // descifra el texto cifrado
        cifrador.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] textoDescifrado = cifrador.doFinal(Base64.getDecoder().decode(textoCifradoBase64));
        String textoOriginalDescifrado = new String(textoDescifrado, "UTF-8");

        System.out.println("Texto descifrado: " + textoOriginalDescifrado);
    }
}
