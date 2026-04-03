package client;
import javax.swing.*;
import java.awt.*;

public class ChatGUI {

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JTextField usernameField;
    private JTextField channelField;

    private JButton sendButton;
    private JButton connectButton;
    private JButton joinButton;
    private JButton listButton;
    private JButton usersButton;

    private ClientCore client;

    public ChatGUI() {
        client = new ClientCore();

        frame = new JFrame("Java Chat");
        frame.setSize(500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Chat alanı 
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Top panel (username + connect)
        JPanel topPanel = new JPanel(new GridLayout(2, 1));

        JPanel connectPanel = new JPanel(new BorderLayout());
        usernameField = new JTextField();
        connectButton = new JButton("Connect");

        connectPanel.add(new JLabel("Username:"), BorderLayout.WEST);
        connectPanel.add(usernameField, BorderLayout.CENTER);
        connectPanel.add(connectButton, BorderLayout.EAST);

        // Channel panel
        JPanel channelPanel = new JPanel(new BorderLayout());
        channelField = new JTextField();
        joinButton = new JButton("Join");

        channelPanel.add(new JLabel("Channel:"), BorderLayout.WEST);
        channelPanel.add(channelField, BorderLayout.CENTER);
        channelPanel.add(joinButton, BorderLayout.EAST);

        topPanel.add(connectPanel);
        topPanel.add(channelPanel);

        frame.add(topPanel, BorderLayout.NORTH);

        // Bottom panel (message + buttons)
        JPanel bottomPanel = new JPanel(new BorderLayout());

        messageField = new JTextField();
        sendButton = new JButton("Send");

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        // Extra butonlar
        JPanel extraPanel = new JPanel(new GridLayout(1, 2));
        listButton = new JButton("Channels");
        usersButton = new JButton("Users");

        extraPanel.add(listButton);
        extraPanel.add(usersButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(bottomPanel, BorderLayout.CENTER);
        southPanel.add(extraPanel, BorderLayout.SOUTH);

        frame.add(southPanel, BorderLayout.SOUTH);

        // Listener bağlama
        client.setMessageListener(new ClientCore.MessageListener() {
            @Override
            public void onMessageReceived(String message) {
                SwingUtilities.invokeLater(() -> {
                    chatArea.append(message + "\n");
                });
            }

            @Override
            public void onConnectionStatusChanged(boolean connected, String message) {
                SwingUtilities.invokeLater(() -> {
                    chatArea.append("[STATUS] " + message + "\n");
                });
            }
        });

        //Button actions

        // Connect
        connectButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                chatArea.append("[SYSTEM] Username boş olamaz\n");
                return;
            }

            boolean ok = client.connect("localhost", 1234, username);
            if (!ok) {
                chatArea.append("[SYSTEM] Bağlantı başarısız\n");
            }
        });

        // Send message
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        // Join channel
        joinButton.addActionListener(e -> {
            try {
                int ch = Integer.parseInt(channelField.getText());
                client.changeChannel(ch);
            } catch (Exception ex) {
                chatArea.append("[SYSTEM] Geçersiz channel\n");
            }
        });

        // List channels
        listButton.addActionListener(e -> client.requestChannelList());

        // Users
        usersButton.addActionListener(e -> client.requestUsers());

        frame.setVisible(true);
    }

    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty()) {
            client.sendMessage(msg);
            messageField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatGUI::new);
    }
}
