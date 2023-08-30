import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
public class Extra {
    public static void main(String[] args) throws Exception {


        //texto a cifrar
        String str = "Van a dormir a ruckj";

        System.out.println("\nOriginal: ");

        System.out.println(str);

        //crear clase
        RSA rsa = new RSA();

        //generamos claves
        rsa.genKeyPair(512);


        String file_private = "/tmp/rsa.pri";
        String file_public = "/tmp/rsa.pub";

        //guardamos
        rsa.saveToDiskPrivateKey("/tmp/rsa.pri");
        rsa.saveToDiskPublicKey("/tmp/rsa.pub");

        //ciframos y imprimimos; se lo devuelve en 'secure'
        String secure = rsa.Encrypt(str);

        System.out.println("\nCifrado:");
        System.out.println(secure);



        //creamos otra clase rsa
        RSA rsa2 = new RSA();

        //no creamos un nuevo par de claves, sino que cargamos el conj. de claves que habiamos guadado
        rsa2.openFromDiskPrivateKey("/tmp/rsa.pri");
        rsa2.openFromDiskPublicKey("/tmp/rsa.pub");

        //pasamos el texto cifrado y lo devuelve decifrado
        String unsecure = rsa2.Decrypt(secure);

        //imprimimos
        System.out.println("\nDescifrado:");
        System.out.println(unsecure);



    }
}