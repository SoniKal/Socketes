package Sekiurity3;

import java.io.Serializable;

public class Mensaje implements Serializable {
    private String remitente;
    private String contenido;

    public Mensaje(String remitente, String contenido) {
        this.remitente = remitente;
        this.contenido = contenido;
    }

    public String getRemitente() {
        return remitente;
    }

    public String getContenido() {
        return contenido;
    }

    @Override
    public String toString() {
        return remitente + ": " + contenido;
    }
}

