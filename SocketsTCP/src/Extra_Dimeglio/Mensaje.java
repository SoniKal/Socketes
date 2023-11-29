package Extra_Dimeglio;

import java.io.Serializable;

public class Mensaje implements Serializable {
    private String mensaje;
    private String usuarioDestino;
    private String usuarioOrigen;

    public Mensaje(String mensaje, String usuarioDestino, String usuarioOrigen) {
        this.mensaje = mensaje;
        this.usuarioDestino = usuarioDestino;
        this.usuarioOrigen = usuarioOrigen;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getUsuarioDestino() {
        return usuarioDestino;
    }

    public void setUsuarioDestino(String usuarioDestino) {
        this.usuarioDestino = usuarioDestino;
    }

    public String getUsuarioOrigen() {
        return usuarioOrigen;
    }

    public void setUsuarioOrigen(String usuarioOrigen) {
        this.usuarioOrigen = usuarioOrigen;
    }
}
