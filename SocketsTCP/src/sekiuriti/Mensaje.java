package sekiuriti;

import java.io.Serializable;

public class Mensaje implements Serializable {
    private String mensajeEncriptado, mensajeHasheado, extra;

    public Mensaje(String mensajeEncriptado, String mensajeHasheado, String nombre) {
        this.mensajeEncriptado = mensajeEncriptado;
        this.mensajeHasheado = mensajeHasheado;
        this.extra = nombre;
    }

    public Mensaje(String mensajeEncriptado, String mensajeHasheado) {
        this.mensajeEncriptado = mensajeEncriptado;
        this.mensajeHasheado = mensajeHasheado;
    }

    public Mensaje(String extra) {
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
