package Sekiurity3;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    private static final String SERVIDOR_IP = "172.16.255.221"; // Cambiar a la dirección IP del servidor si es necesario
    private static final int PUERTO = 12345;

    public static void main(String[] args) {
        try {
            Socket socketCliente = new Socket(SERVIDOR_IP, PUERTO);
            System.out.println("Conectado al servidor en " + SERVIDOR_IP + ":" + PUERTO);

            BufferedReader in = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            PrintWriter out = new PrintWriter(socketCliente.getOutputStream(), true);

            Scanner scanner = new Scanner(System.in);

            String mensajeTexto;

            while (true) {
                System.out.print("Ingrese un mensaje (o 'salir' para desconectarse): ");
                mensajeTexto = scanner.nextLine();

                if ("salir".equalsIgnoreCase(mensajeTexto)) {
                    break;
                }

                out.println(mensajeTexto);
            }

            socketCliente.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

