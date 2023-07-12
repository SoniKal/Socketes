import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class TCPServidor {
    public static void main(String args[]){


        Socket s=null;
        ServerSocket ss2=null;
        System.out.println("- Servidor activo");
        try{
            ss2 = new ServerSocket(4445); // can also use static final PORT_NUM , when defined

        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("Error de Servidor");

        }

        while(true){
            try{
                s= ss2.accept();
                System.out.println("Conexion Establecida");
                ServerThread st=new ServerThread(s);
                st.start();

            }

            catch(Exception e){
                e.printStackTrace();
                System.out.println("Error de Conexion");

            }
        }

    }

}

class ServerThread extends Thread{

    String line=null;
    BufferedReader  is = null;
    PrintWriter os=null;
    Socket s=null;

    public ServerThread(Socket s){
        this.s=s;
    }

    public void run() {
        try{
            is= new BufferedReader(new InputStreamReader(s.getInputStream()));
            os=new PrintWriter(s.getOutputStream());

        }catch(IOException e){
            System.out.println("Error de IO en thread de servidor");
        }

        try {
            line=is.readLine();
            while(line.compareTo("Salir")!=0){

                os.println(line);
                os.flush();
                System.out.println("Respuesta a Cliente  :  "+line);
                line=is.readLine();
            }
        } catch (IOException e) {

            line=this.getName(); //reused String line for getting thread name
            System.out.println("Error IO / Cliente "+line+" terminado abruptamentr");
        }
        catch(NullPointerException e){
            line=this.getName(); //reused String line for getting thread name
            System.out.println("Cliente "+line+" cerrado");
        }

        finally{
            try{
                System.out.println("Conexion Cerrada-");
                if (is!=null){
                    is.close();
                    System.out.println(" Socket Input Stream Closed");
                }

                if(os!=null){
                    os.close();
                    System.out.println("Socket Out Closed");
                }
                if (s!=null){
                    s.close();
                    System.out.println("Socket Closed");
                }

            }
            catch(IOException ie){
                System.out.println("Socket Close Error");
            }
        }//end finally
    }
}
