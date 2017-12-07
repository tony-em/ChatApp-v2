import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerWindow extends JFrame {

    private JPanel panel;
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private ServerConnection serverConnection;

    public ServerWindow(String title, ServerConnection serverConnection) throws HeadlessException {
        super(title);
        this.serverConnection = serverConnection;
        setupUI();
    }

    private void setupUI() {
        panel = new JPanel();
        panel.setLayout(new FormLayout("fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow", "center:d:grow,top:4dlu:noGrow,center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:grow"));
        ipField = new JTextField();
        CellConstraints cc = new CellConstraints();
        panel.add(ipField, cc.xyw(5, 3, 6, CellConstraints.FILL, CellConstraints.DEFAULT));
        portField = new JTextField();
        panel.add(portField, cc.xyw(5, 5, 6, CellConstraints.FILL, CellConstraints.DEFAULT));
        connectButton = new JButton();
        connectButton.setText("Connect");
        connectButton.addActionListener(connectListener);
        panel.add(connectButton, cc.xywh(5, 9, 5, 1));

        final JLabel label1 = new JLabel();
        label1.setText("Port");
        panel.add(label1, cc.xy(3, 5));
        final JLabel label2 = new JLabel();
        label2.setText("IP");
        panel.add(label2, cc.xy(3, 3));
        getContentPane().add(panel);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(450, 300);
        setResizable(false);
        setVisible(true);
    }

    private void showMsgBox(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Message", JOptionPane.INFORMATION_MESSAGE);
    }

    ActionListener connectListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            connectButton.setEnabled(false);
            String port = portField.getText().trim(), ip = ipField.getText().trim();
            if (port.isEmpty() || ip.isEmpty()) {
                showMsgBox("Enter server information");
                connectButton.setEnabled(true);
                return;
            }

            if (serverConnection.connect(ip, port)) {
                new LoginWindow("Login", serverConnection);
                setVisible(false);
            } else {
                showMsgBox("Server " + ip + ":" + port + " is not found");
                connectButton.setEnabled(true);
            }
        }
    };
}
