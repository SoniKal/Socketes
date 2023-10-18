package Tcp_key_test;

import java.io.Serializable;

public class Mensaje_2 implements Serializable {
    private String mensaje_encriptado;

    public Mensaje_2(String mensaje_encriptado) {
        this.mensaje_encriptado = mensaje_encriptado;
    }

    public String getMensaje_encriptado() {
        return mensaje_encriptado;
    }

    public void setMensaje_encriptado(String mensaje_encriptado) {
        this.mensaje_encriptado = mensaje_encriptado;
    }
}
