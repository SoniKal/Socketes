package TCP_recup;

import java.io.Serializable;

public class Mensaje implements Serializable {
    private String texto;
    private String destino;

    public Mensaje(String texto, String destino) {
        this.texto = texto;
        this.destino = destino;
    }

    public String getTexto() {
        return texto;
    }

    public String getDestino() {
        return destino;
    }
}
