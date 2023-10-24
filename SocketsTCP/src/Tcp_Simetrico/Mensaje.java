package Tcp_Simetrico;

import java.io.Serializable;
public class Mensaje implements Serializable {
    private String mensajeEncriptado;
    private String mensajeHasheado;
    private String extra; // agregamos la clave pública

    public Mensaje(String mensajeEncriptado, String mensajeHasheado) {
        this.mensajeEncriptado = mensajeEncriptado;
        this.mensajeHasheado = mensajeHasheado;
    }

    public Mensaje(String mensajeEncriptado, String mensajeHasheado, String extra) {
        this.mensajeEncriptado = mensajeEncriptado;
        this.mensajeHasheado = mensajeHasheado;
        this.extra = extra;
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

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}







