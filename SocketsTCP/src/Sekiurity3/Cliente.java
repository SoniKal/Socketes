package Sekiurity3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Cliente {
    private static final String SERVIDOR_IP = "localhost"; // Cambiar a la IP del servidor si es necesario
    private static final int PUERTO = 6969;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new ChatClientFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}

class ChatClientFrame extends JFrame {
    private static final String SERVIDOR_IP = "172.16.255.221"; // Cambiar a la IP del servidor si es necesario
    private static final int PUERTO = 6969;

    private Socket socket;
    private ObjectOutputStream out;

    private JTextField messageField;
    private JTextArea chatArea;

    public ChatClientFrame() {
        setTitle("Cliente de Chat");
        setSize(400, 300);
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        messageField = new JTextField();
        messageField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage(messageField.getText());
            }
        });
        add(messageField, BorderLayout.SOUTH);

        try {
            socket = new Socket(SERVIDOR_IP, PUERTO);
            out = new ObjectOutputStream(socket.getOutputStream());

            Thread recibirMensajes = new Thread(new Runnable() {
                public void run() {
                    try {
                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                        while (true) {
                            Mensaje mensajeRecibido = (Mensaje) in.readObject();
                            chatArea.append(mensajeRecibido.toString() + "\n");
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
            recibirMensajes.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        try {
            Mensaje mensaje = new Mensaje(socket.getLocalAddress().getHostAddress(), message);
            out.writeObject(mensaje);
            out.flush();
            messageField.setText("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}






