package sekiuriti;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class Mensaje implements Serializable {
    private String texto;
    private byte[] firma; // Firma digital del mensaje

    public Mensaje(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }

    public void firmar(PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(texto.getBytes());
            firma = signature.sign();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean verificarFirma(PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(texto.getBytes());
            return signature.verify(firma);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        return texto;
    }

    public String obtenerFirmaComoString() {
        return Base64.getEncoder().encodeToString(firma);
    }
}
