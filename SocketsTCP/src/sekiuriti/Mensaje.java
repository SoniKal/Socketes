package sekiuriti;

import java.security.*;
import java.util.Base64;

public class Mensaje {
    private String texto;
    private byte[] firma;

    public Mensaje(String texto, PrivateKey privateKey) throws Exception {
        this.texto = texto;
        this.firma = firmarTexto(texto, privateKey).getBytes();
    }

    public String getTexto() {
        return texto;
    }

    public byte[] getFirma() {
        return firma;
    }

    public static String firmarTexto(String texto, PrivateKey privateKey) throws Exception {
        Signature firma = Signature.getInstance("SHA256withRSA");
        firma.initSign(privateKey);
        firma.update(texto.getBytes());
        byte[] firmaBytes = firma.sign();
        return Base64.getEncoder().encodeToString(firmaBytes);
    }

    public static boolean verificarFirma(String texto, byte[] firmaBytes, PublicKey publicKey) throws Exception {
        Signature firma = Signature.getInstance("SHA256withRSA");
        firma.initVerify(publicKey);
        firma.update(texto.getBytes());
        return firma.verify(firmaBytes);
    }
}
