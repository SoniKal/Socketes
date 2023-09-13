package sekiuriti;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Utilidades {

    // Convierte una clave pública desde su representación Base64 a PublicKey
    public static PublicKey convertirClavePublicaDesdeBase64(String clavePublicaBase64) {
        try {
            byte[] claveBytes = Base64.getDecoder().decode(clavePublicaBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(claveBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Convierte una clave pública a su representación Base64
    public static String convertirClavePublicaABase64(PublicKey clavePublica) {
        byte[] claveBytes = clavePublica.getEncoded();
        return Base64.getEncoder().encodeToString(claveBytes);
    }
}