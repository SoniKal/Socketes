package Sekiurity3;

public class Mensaje {
    private String remitente;
    private String contenido;

    public Mensaje(String remitente, String contenido) {
        this.remitente = remitente;
        this.contenido = contenido;
    }

    @Override
    public String toString() {
        return remitente + ": " + contenido;
    }
}

