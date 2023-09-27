package Sekiurity3;

import java.io.Serializable;

// Clase para representar un mensaje
public class Mensaje implements Serializable {
    private String emisor; // IP del emisor
    private String texto;  // Texto del mensaje

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
}





