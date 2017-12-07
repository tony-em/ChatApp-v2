import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MessagingService extends Thread {

    private Socket clientSocket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private volatile boolean enable;

    private HelperDB helperDB;
    private SQLiteUserDAO userDAO;

    private static List<MessagingService> clients = Collections.synchronizedList(new ArrayList<MessagingService>());

    public static final int RESPONSE_REGISTRATION_ERROR_DATA_CORRECT = 100;
    public static final int RESPONSE_REGISTRATION_ERROR_USER_CREATED = 101;
    public static final int RESPONSE_REGISTRATION_OK = 102;
    public static final int RESPONSE_AUTHORIZATION_ERROR_DATA_CORRECT = 200;
    public static final int RESPONSE_AUTHORIZATION_ERROR_USER_NOTCREATED = 201;
    public static final int RESPONSE_AUTHORIZATION_ERROR_USER_AUTORIZED = 202;
    public static final int RESPONSE_AUTHORIZATION_OK = 203;
    public static final int RESPONSE_ERROR_CODE = 402;

    public static final int REQUEST_REGISTRATION = 1;
    public static final int REQUEST_AUTHORIZATION = 2;

    public MessagingService(Socket socket, HelperDB helperDB) throws IOException {
        clientSocket = socket;
        dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        this.helperDB = helperDB;
        try {
            createTable();
        } catch (Exception e) {
            System.out.println("ERROR_DB: Table is not created!");
        }

        userDAO = new SQLiteUserDAO(helperDB);
    }

    private void createTable() throws SQLException, IOException, URISyntaxException {
        Statement s;
        try {
            helperDB.openConnection(ChatServer.JDBC_URL, ChatServer.DB_USERNAME, ChatServer.DB_PASSWORD);
            s = helperDB.getStatement();
            s.executeUpdate(readResource(ChatServer.CREATE_TABLE_FILENAME));
        } finally {
            try {
                helperDB.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readResource(String path) throws URISyntaxException, IOException {

        // работает только в src
//        URL url = ClassLoader.getSystemResource(path);
//        URL url = Main.class.getResource(path);
//        URL url = ClassLoader.getSystemClassLoader().getResource(path);
//        Path p = Paths.get(url.toURI());

        Path p = Paths.get(path); // работает как на уровне src так и под ним
        return new String(Files.readAllBytes(p), "UTF8");
    }

    @Override
    public void run() {
        while (true) {
            try {
                String cmd = dataInputStream.readUTF();

                boolean OK = false;
                String[] parts = cmd.split("&");
                if (parts.length != 4) {
                    sendResponse(RESPONSE_ERROR_CODE);
                    continue;
                }

                int intent = Integer.parseInt(parts[3]);
                String email = parts[0];
                String nickname = parts[1];
                String pass = parts[2];

                // query select
                boolean userIsCreated = false;

                switch (intent) {
                    case REQUEST_REGISTRATION:
                        try {
                            int res = userDAO.addUser(nickname, pass, email);
                            if (res == 0) {
                                sendResponse(RESPONSE_REGISTRATION_OK);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        break;
                    case REQUEST_AUTHORIZATION:
                        try {
                            User user = userDAO.validateUser(nickname, pass, email);
                            if (user != null) {
                                sendResponse(RESPONSE_AUTHORIZATION_OK);
                                OK = true;
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        sendResponse(RESPONSE_ERROR_CODE);
                        continue;
                }

                if (OK) break;

            } catch (IOException e) {
                try {
                    clientSocket.close();
                    dataOutputStream.close();
                    dataInputStream.close();
                } catch (IOException io) {
                }
                System.out.println("Client " + this.clientSocket.getInetAddress() + " disconnected.");
                return;
            }
        }

        enable = true;
        try {
            clients.add(this);

            while (enable) {
                LocalTime localTime = LocalTime.now();
                String nowTime = localTime.getHour() + ":" + localTime.getMinute() + ":" + localTime.getSecond() + " ";
                String msg = nowTime + dataInputStream.readUTF().trim();
                sharing(msg);
            }

        } catch (IOException io) {
            System.out.println("Client " + this.clientSocket.getInetAddress() + " disconnected.");
        } finally {
            clients.remove(this);

            try {
                clientSocket.close();
                dataOutputStream.close();
                dataInputStream.close();
            } catch (IOException io) {
            }
        }
    }

    private void sendResponse(int response) throws IOException {
        dataOutputStream.writeUTF(String.valueOf(response));
        dataOutputStream.flush();
    }

    private static void sharing(String msg) {
        synchronized (clients) {
            Iterator<MessagingService> iter = clients.iterator();
            while (iter.hasNext()) {
                MessagingService client = iter.next();

                try {
                    synchronized (client.dataOutputStream) {
                        client.dataOutputStream.writeUTF(msg);
                    }
                    client.dataOutputStream.flush();
                } catch (IOException io) {
                    client.enable = false;
                }
            }
        }
    }
}
