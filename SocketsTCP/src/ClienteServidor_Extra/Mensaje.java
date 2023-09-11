package ClienteServidor_Extra;

import java.io.Serializable;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class Mensaje implements Serializable{
    private String mensajeHasheado;
    private String mensajeEncriptado;
    private PublicKey llavePublica;

    public Mensaje(String mensajeHasheado, String mensajeEncriptado, PublicKey llavePublica) {
        this.mensajeHasheado = mensajeHasheado;
        this.mensajeEncriptado = mensajeEncriptado;
        this.llavePublica = llavePublica;
    }

    public Mensaje() {
        this.mensajeHasheado = "DummyHash";
        this.mensajeEncriptado = "DummyEncrypt";
    }

    public String getMensajeHasheado() {
        return mensajeHasheado;
    }

    public void setMensajeHasheado(String mensajeHasheado) {
        this.mensajeHasheado = mensajeHasheado;
    }

    public String getMensajeEncriptado() {
        return mensajeEncriptado;
    }

    public void setMensajeEncriptado(String mensajeEncriptado) {
        this.mensajeEncriptado = mensajeEncriptado;
    }

    public PublicKey getLlavePublica() {
        return llavePublica;
    }

    public void setLlavePublica(PublicKey llavePublica) {
        this.llavePublica = llavePublica;
    }
}
