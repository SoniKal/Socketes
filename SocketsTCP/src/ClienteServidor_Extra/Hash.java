package ClienteServidor_Extra;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.*;

public class Hash {
    public String hashear(String mensaje) {
        try {
            // creo instancia de algoritmo hash 'sha-256'
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // pasaje de string -> bytes
            byte[] encodedhash = digest.digest(
                    mensaje.getBytes(StandardCharsets.UTF_8));

            // convierto los bits pasados en hexadecimal
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            // excepciones en caso de errores
            e.printStackTrace();
            return null;
        }
    }
}
