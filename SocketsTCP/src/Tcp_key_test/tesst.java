import TCP_Firma.Hash;
import TCP_Firma.Mensaje;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Scanner;

public class tesst {
    private SecretKey key;
    private final int KEY_SIZE = 128;
    private final int T_LEN = 128;
    private Cipher encryptionCipher;
    public void  init() {
        Thread Empezar = new Thread(() -> {
            try {
                String algorithm = "AES"; // Specify the algorithm (e.g., AES)
                SecretKey secret;
                KeyGenerator generator = KeyGenerator.getInstance("AES");
                generator.init(256);
                secret = generator.generateKey(); //Genera la llave


                System.out.println(secret + " " + "la llave al inicio");
                String llave = secretKeyToString(secret);
                System.out.println(llave+" "+"la llave convert en string");

                byte[] encriptado = encryptData(llave,secret);
                String salida = decryptData(encriptado,secret);
                SecretKey finl = stringToSecretKey(salida,algorithm);
                System.out.println(finl+" "+"es la llave final"); //funcional


            } catch (Exception ignore) {
            }
        });
        Empezar.start();
        }


    public static String secretKeyToString (SecretKey secretKey){
        byte[] keyBytes = secretKey.getEncoded();
        return Base64.getEncoder().encodeToString(keyBytes);
    } //de llave a string

    public static SecretKey stringToSecretKey (String keyString, String algorithm){
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyString);
            return new SecretKeySpec(keyBytes, algorithm);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }//de string a llave

    public  byte[] encryptData(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }
    public String decryptData(byte[] encryptedData, SecretKey key) throws Exception {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
    }


    public static void main(String[] args) {
        tesst t = new tesst();
        t.init();
    }
}



