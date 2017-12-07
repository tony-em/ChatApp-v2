import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ServerConnection {

    private static final int TIMEOUT_CONNECTION = 1000;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public boolean connect(String ipServer, String port) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ipServer, Integer.parseInt(port)), TIMEOUT_CONNECTION);
            System.out.println("Connecting successful!");
        } catch (Exception io) {
            clear();
            return false;
        }

        try {
            dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (Exception io) {

            try {
                socket.close();
                if (dataInputStream != null) dataInputStream.close();
            } catch (Exception e) {
            }

            clear();
            return false;
        }

        return true;
    }

    private void clear() {
        socket = null;
        dataInputStream = null;
        dataOutputStream = null;
    }

    public Socket getSocket() {
        return socket;
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }
}
