public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            throw new RuntimeException("Set two params: <IP_SERVER> <PORT> <NICKNAME>");
        }

        new ServerWindow("Server information", new ServerConnection());
    }
}