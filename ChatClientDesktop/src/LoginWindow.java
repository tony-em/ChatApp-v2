import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginWindow extends JFrame {

    private JPanel panel;
    private JTextField emailField;
    private JTextField nicknameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registrationButton;
    private ServerConnection serverConnection;

    public static final int RESPONSE_REGISTRATION_ERROR_DATA_CORRECT = 100;
    public static final int RESPONSE_REGISTRATION_ERROR_USER_CREATED = 101;
    public static final int RESPONSE_REGISTRATION_OK = 102;
    public static final int RESPONSE_AUTHORIZATION_ERROR_DATA_CORRECT = 200;
    public static final int RESPONSE_AUTHORIZATION_ERROR_USER_NOT_CREATED = 201;
    public static final int RESPONSE_AUTHORIZATION_ERROR_USER_AUTHORIZED = 202;
    public static final int RESPONSE_AUTHORIZATION_OK = 203;
    public static final int RESPONSE_ERROR_CODE = 402;

    public static final int REQUEST_REGISTRATION = 1;
    public static final int REQUEST_AUTHORIZATION = 2;

    private MessageDigest crypt;

    private boolean reconnectFlag = false;

    public LoginWindow(String title, ServerConnection serverConnection) throws HeadlessException {
        super(title);
        this.serverConnection = serverConnection;
        setupUI();
        try {
            crypt = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void setupUI() {
        panel = new JPanel();
        panel.setLayout(new FormLayout("fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow", "center:d:grow,top:4dlu:noGrow,center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:grow"));
        emailField = new JTextField();
        CellConstraints cc = new CellConstraints();
        panel.add(emailField, cc.xyw(5, 3, 6, CellConstraints.FILL, CellConstraints.DEFAULT));
        nicknameField = new JTextField();
        panel.add(nicknameField, cc.xyw(5, 5, 6, CellConstraints.FILL, CellConstraints.DEFAULT));
        passwordField = new JPasswordField();
        panel.add(passwordField, cc.xyw(5, 7, 6, CellConstraints.FILL, CellConstraints.DEFAULT));
        loginButton = new JButton();
        loginButton.setText("Login");
        loginButton.addActionListener(loginListener);
        panel.add(loginButton, cc.xywh(5, 9, 4, 2));

        final JLabel label1 = new JLabel();
        label1.setText("Nickname");
        panel.add(label1, cc.xy(3, 5));
        final JLabel label2 = new JLabel();
        label2.setText("Email");
        panel.add(label2, cc.xy(3, 3));
        final JLabel label3 = new JLabel();
        label3.setText("Password");
        panel.add(label3, cc.xy(3, 7));

        registrationButton = new JButton();
        registrationButton.setText("Registration");
        registrationButton.addActionListener(registerListener);
        panel.add(registrationButton, cc.xy(10, 9, CellConstraints.DEFAULT, CellConstraints.CENTER));
        getContentPane().add(panel);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(450, 300);
        setResizable(false);
        setVisible(true);
    }

    private void showMsgBox(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Message", JOptionPane.INFORMATION_MESSAGE);
    }

    private void request(int cmd, String email, String nickname, String pass) throws IOException {
        serverConnection.getDataOutputStream().writeUTF(email + "&&&" + nickname + "&&&" + pass + "&&&" + String.valueOf(cmd));
        serverConnection.getDataOutputStream().flush();
    }

    private int response() throws IOException {
        return Integer.parseInt(serverConnection.getDataInputStream().readUTF());
    }

    private void exec(int cmd) {
        if (reconnectFlag) {
            if (!serverConnection.connect(serverConnection.getIp(), serverConnection.getPort())) {
                showMsgBox("Server is not found");
                return;
            }
            reconnectFlag = false;
        }

        String email = emailField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (email.isEmpty() || nickname.isEmpty() || pass.isEmpty()) {
            showMsgBox("Enter all fields");
            return;
        }

        crypt.update(pass.getBytes());
        byte[] passMD5 = crypt.digest();
        pass = DatatypeConverter.printHexBinary(passMD5).toUpperCase();

        try {
            System.out.println(email + nickname + pass);
            request(cmd, email, nickname, pass);
        } catch (IOException e) {
            showMsgBox("Server is not found");
            reconnectFlag = true;
            return;
        }

        int response;
        try {
            response = response();
        } catch (IOException e) {
            showMsgBox("Server is not found");
            reconnectFlag = true;
            return;
        }

        switch (response) {
            case RESPONSE_AUTHORIZATION_OK:
                new ChatWindow(serverConnection.getSocket().getInetAddress().getHostAddress(),
                        String.valueOf(serverConnection.getSocket().getPort()),
                        serverConnection, nickname);
                setVisible(false);
                break;
            case RESPONSE_REGISTRATION_OK:
                showMsgBox("Registration is successful. You can a login!");
                break;
            case RESPONSE_REGISTRATION_ERROR_DATA_CORRECT:
                showMsgBox("Fields uncorrected");
                break;
            case RESPONSE_REGISTRATION_ERROR_USER_CREATED:
                showMsgBox("This user already exists");
                break;
            case RESPONSE_AUTHORIZATION_ERROR_DATA_CORRECT:
                showMsgBox("Fields uncorrected");
                break;
            case RESPONSE_AUTHORIZATION_ERROR_USER_NOT_CREATED:
                showMsgBox("This user does not exist");
                break;
            case RESPONSE_ERROR_CODE:
                showMsgBox("Request is uncorrected");
                break;
            case RESPONSE_AUTHORIZATION_ERROR_USER_AUTHORIZED:
                showMsgBox("This user is already authorized");
                break;
            default:
                showMsgBox("Server is not found");
                break;
        }
    }

    ActionListener registerListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            registrationButton.setEnabled(false);
            exec(REQUEST_REGISTRATION);
            registrationButton.setEnabled(true);
        }
    };

    ActionListener loginListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            loginButton.setEnabled(false);
            exec(REQUEST_AUTHORIZATION);
            loginButton.setEnabled(true);
        }
    };
}
