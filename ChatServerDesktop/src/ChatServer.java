import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {

    public static final String DB_NAME = "users.db";
    public static final String JDBC_DRIVER = "org.sqlite.JDBC";
    public static final String JDBC_URL = "jdbc:sqlite:" + DB_NAME;
    public static final String DB_USERNAME = "";
    public static final String DB_PASSWORD = "";
    public static final String CREATE_TABLE_FILENAME = "create_table_users_sqlite.sql";

    public ChatServer(int port) {
        HelperDB helperDB = HelperDB.getInstance();
        try {
            helperDB.initDriver(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is run...");
            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected: " + socket.getInetAddress());
                    new MessagingService(socket, helperDB).start();
                }
            } catch (IOException io) {
                io.printStackTrace();
            } finally {
                serverSocket.close();
            }

        } catch (IOException io) {
            io.printStackTrace();
            throw new RuntimeException("Server is not run! Set new server configuration.");
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new RuntimeException("Set params server configuration for run: <PORT>");
        }

        new ChatServer(Integer.parseInt(args[0]));
    }
}
