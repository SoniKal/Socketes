import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Firma {

    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

    public Firma() throws KeyStoreException {
    }

    public static void main(String[] args) throws Exception {
        String message = "Este es un mensaje para firmar.";

        String privateKeyStr = "<-CLAVE PRIVADA INICIO->\n" + "<-CLAVE PRIVADA FIN->";

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);

        signature.update(message.getBytes());

        byte[] digitalSignature = signature.sign();

        String signatureBase64 = Base64.getEncoder().encodeToString(digitalSignature);
        System.out.println("Firma digital: " + signatureBase64);
    }
}
