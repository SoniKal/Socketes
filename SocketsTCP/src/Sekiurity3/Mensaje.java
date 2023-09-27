package Sekiurity3;

import java.io.Serializable;

public class Mensaje implements Serializable {
    private String emisor;
    private String texto;

    public Mensaje(String emisor, String texto) {
        this.emisor = emisor;
        this.texto = texto;
    }

    public String getEmisor() {
        return emisor;
    }

    public String getTexto() {
        return texto;
    }

    @Override
    public String toString() {
        return emisor + ": " + texto;
    }
}




