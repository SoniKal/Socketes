package Sekiurity3;

import java.io.Serializable;
import java.security.PublicKey;

public class Mensaje implements Serializable {
    private String mensajeEncriptado;
    private String mensajeHasheado;
    private PublicKey publicKey; // agregamos la clave pública

    public Mensaje(String mensajeEncriptado, String mensajeHasheado) {
        this.mensajeEncriptado = mensajeEncriptado;
        this.mensajeHasheado = mensajeHasheado;
    }

    public Mensaje(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public String getMensajeEncriptado() {
        return mensajeEncriptado;
    }

    public void setMensajeEncriptado(String mensajeEncriptado) {
        this.mensajeEncriptado = mensajeEncriptado;
    }

    public String getMensajeHasheado() {
        return mensajeHasheado;
    }

    public void setMensajeHasheado(String mensajeHasheado) {
        this.mensajeHasheado = mensajeHasheado;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }
}






