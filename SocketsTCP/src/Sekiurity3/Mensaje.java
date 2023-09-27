package Sekiurity3;

import java.io.Serializable;
import java.security.PublicKey;

public class Mensaje implements Serializable {
    private String mensajeEncriptado;
    private String firma;
    private PublicKey publicKey; // Agregamos la clave pública

    public Mensaje(String mensajeEncriptado, String firma) {
        this.mensajeEncriptado = mensajeEncriptado;
        this.firma = firma;
        this.publicKey = publicKey;
    }

    public String getMensajeEncriptado() {
        return mensajeEncriptado;
    }

    public String getFirma() {
        return firma;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}






