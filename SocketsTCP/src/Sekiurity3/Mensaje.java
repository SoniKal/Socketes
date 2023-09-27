package Sekiurity3;

import java.io.Serializable;

public class Mensaje implements Serializable {
    private String mensajeEncriptado;
    private String firma;

    public Mensaje(String mensajeEncriptado, String firma) {
        this.mensajeEncriptado = mensajeEncriptado;
        this.firma = firma;
    }

    public String getMensajeEncriptado() {
        return mensajeEncriptado;
    }

    public void setMensajeEncriptado(String mensajeEncriptado) {
        this.mensajeEncriptado = mensajeEncriptado;
    }

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        this.firma = firma;
    }
}






