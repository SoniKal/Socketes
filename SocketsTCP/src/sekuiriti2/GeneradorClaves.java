package sekuiriti2;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

public class GeneradorClaves {
    public static KeyPair generarParClaves() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048); // Tamaño de clave recomendado para seguridad
        return keyGen.generateKeyPair();
    }

    public static PublicKey claveDesdeString(String clave) throws Exception {
        byte[] claveBytes = Base64.getDecoder().decode(clave);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new X509EncodedKeySpec(claveBytes));
    }

    public static String claveAString(Key clave) {
        return Base64.getEncoder().encodeToString(clave.getEncoded());
    }

    public static byte[] encriptarConClavePublica(String mensaje, PublicKey clavePublica) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, clavePublica);
        return cipher.doFinal(mensaje.getBytes());
    }

    public static String firmarMensaje(String mensaje, PrivateKey clavePrivada) throws Exception {
        Signature firma = Signature.getInstance("SHA256withRSA");
        firma.initSign(clavePrivada);
        firma.update(mensaje.getBytes());
        byte[] firmaBytes = firma.sign();
        return Base64.getEncoder().encodeToString(firmaBytes);
    }

    public static boolean verificarFirma(String mensaje, String firma, PublicKey clavePublica) throws Exception {
        Signature verificarFirma = Signature.getInstance("SHA256withRSA");
        verificarFirma.initVerify(clavePublica);
        verificarFirma.update(mensaje.getBytes());
        byte[] firmaBytes = Base64.getDecoder().decode(firma);
        return verificarFirma.verify(firmaBytes);
    }
}

