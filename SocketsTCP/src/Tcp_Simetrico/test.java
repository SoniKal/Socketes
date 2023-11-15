package Tcp_Simetrico;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class test {
    public static void main(String[] args) throws IOException {
        String texto = "";
        try {
            BufferedReader bf = new BufferedReader(new FileReader("/home/fabricio_fiesta/Labo_2023 CSTCB/tp_redes/Socketes/SocketsTCP/src/TCP_recup/Topo"));
            String temp = "";
            String bfRead;
            while ((bfRead = bf.readLine()) != null) {
                System.out.println(bfRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
