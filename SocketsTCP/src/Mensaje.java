import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class Mensaje {
    private String mensaje;
    private PublicKey claveServidor;
    private PrivateKey claveEmisor;

    public Mensaje() {
        this.mensaje = "Dummy";
        this.claveServidor = null;
        this.claveEmisor = null;
    }

    public Mensaje(String mensaje, PublicKey claveServidor, PrivateKey claveEmisor) {
        this.mensaje = mensaje;
        this.claveServidor = claveServidor;
        this.claveEmisor = claveEmisor;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public PublicKey getClaveServidor() {
        return claveServidor;
    }

    public void setClaveServidor(PublicKey claveServidor) {
        this.claveServidor = claveServidor;
    }

    public PrivateKey getClaveEmisor() {
        return claveEmisor;
    }

    public void setClaveEmisor(PrivateKey claveEmisor) {
        this.claveEmisor = claveEmisor;
    }
}
