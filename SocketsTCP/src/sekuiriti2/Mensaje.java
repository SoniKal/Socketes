package sekuiriti2;

public class Mensaje {
    private String mensajeEncriptado;
    private String firma;

    public Mensaje(String mensajeEncriptado, String firma) {
        this.mensajeEncriptado = mensajeEncriptado;
        this.firma = firma;
    }

    public String getMensajeEncriptado() {
        return mensajeEncriptado;
    }

    public String getFirma() {
        return firma;
    }
}
