import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private ServerSocket serverSocket;
    private Socket socket;
    private InputStream input;
    private BufferedReader buffer;
    private String port = "127.0.0.1";

    public TCPServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public TCPServer() throws IOException {
        this.serverSocket = new ServerSocket(6868);
    }

    public void socketAccept() throws IOException {
        socket = serverSocket.accept(); //bloquea conexiones
        input = socket.getInputStream(); //recibe los datos del cliente conectado
        buffer = new BufferedReader(new InputStreamReader(input)); //los convierte en strings

    }
}
