package Extra_Dimeglio;

public class Main {
    public static void main(String[] args) {
        Usuario usuario = new Usuario();
        String publica = Usuario.interfazIP();

        if (publica != null) {
            System.out.println("----------------------------------------------------------");
            System.out.println("Dir. IP de Interfaz: " + publica);
        } else {
            System.err.println("[ERROR OBTENCION INTERFAZ]");
            return;
        }

        usuario.importarTXT("/home/jorge/Escritorio/Socketes/SocketsTCP/src/Extra_Dimeglio/topologia");

        int posicion = -1;
        int index = 0;
        for (Usuario u : usuario.getUsuarios()) {
            if (u.getDireccionIP().equals(publica)) {
                posicion = index + 1;
                break;
            }
            index++;
        }

        if (posicion != -1) {
            System.out.println("----------------------------------------------------------");
            System.out.println("Usuario: " + usuario.getUsuarios().get(posicion-1).getNombreUsuario());
            System.out.println("Dir. IP: " + usuario.getUsuarios().get(posicion-1).getDireccionIP());
            System.out.println("Puerto: " + usuario.getUsuarios().get(posicion-1).getPuertoUsuario());
            System.out.println("Posicion: [" + posicion+"]");
            System.out.println("----------------------------------------------------------");
            System.out.println("Topologia: ");
            for (Usuario z : usuario.getUsuarios())
            {
                System.out.println(z.getNombreUsuario() + ": " + z.getDireccionIP());
            }
            System.out.println("----------------------------------------------------------");
        } else {
            System.err.println("[POSICION NO ENCONTRADA]");
            return;
        }
    }
}