package sekiuriti;

import java.io.Serializable;

public class Mensaje implements Serializable {
    private String texto;

    public Mensaje(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }
}

