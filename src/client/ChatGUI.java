package client;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

public class ChatGUI {

    private JFrame frame;
    private JTextPane chatArea; 
    private JTextField messageField;
    private JTextField usernameField;
    private JTextField ipField; 
    private JTextField channelField;

    private JButton sendButton;
    private JButton connectButton;
    private JButton joinButton;
    private JButton listButton;
    private JButton usersButton;

    private ClientCore client;

    public ChatGUI() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        client = new ClientCore();

        frame = new JFrame("Java Channel-Based Wireless Chat System");
        frame.setSize(650, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // Üst Panel: IP, Username ve Bağlantı
        JPanel northPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        northPanel.setBorder(new EmptyBorder(10, 10, 5, 10));

        JPanel connectPanel = new JPanel(new BorderLayout(5, 5));
        ipField = new JTextField("127.0.0.1", 10);
        usernameField = new JTextField();
        connectButton = new JButton("Connect");
        
        JPanel ipInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ipInputPanel.add(new JLabel("Server IP:"));
        ipInputPanel.add(ipField);
        ipInputPanel.add(new JLabel("Username:"));
        
        connectPanel.add(ipInputPanel, BorderLayout.WEST);
        connectPanel.add(usernameField, BorderLayout.CENTER);
        connectPanel.add(connectButton, BorderLayout.EAST);

        // Kanal Paneli
        JPanel channelPanel = new JPanel(new BorderLayout(5, 5));
        channelField = new JTextField("16");
        joinButton = new JButton("Switch Channel");
        channelPanel.add(new JLabel("Channel (1-100):"), BorderLayout.WEST);
        channelPanel.add(channelField, BorderLayout.CENTER);
        channelPanel.add(joinButton, BorderLayout.EAST);

        northPanel.add(connectPanel);
        northPanel.add(channelPanel);
        frame.add(northPanel, BorderLayout.NORTH);

        // Orta Panel: Chat Alanı
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(new TitledBorder("Communication Log"));
        frame.add(scrollPane, BorderLayout.CENTER);

        // Alt Panel: Mesaj Yazma ve Butonlar
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        JPanel msgInputPanel = new JPanel(new BorderLayout(5, 5));
        messageField = new JTextField();
        messageField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sendButton = new JButton("Send Message");
        msgInputPanel.add(new JLabel("Your Message:"), BorderLayout.WEST);
        msgInputPanel.add(messageField, BorderLayout.CENTER);
        msgInputPanel.add(sendButton, BorderLayout.EAST);

        JPanel actionButtonsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        listButton = new JButton("List All Channels");
        usersButton = new JButton("Who's in Channel?");
        actionButtonsPanel.add(listButton);
        actionButtonsPanel.add(usersButton);

        southPanel.add(msgInputPanel, BorderLayout.CENTER);
        southPanel.add(actionButtonsPanel, BorderLayout.SOUTH);
        frame.add(southPanel, BorderLayout.SOUTH);

        // Mesaj Dinleyici
        client.setMessageListener(new ClientCore.MessageListener() {
            @Override
            public void onMessageReceived(String message) {
                // "Enter username" kalıntısını burada filtreliyoruz
                if (message.contains("Enter username")) return; 
                
                if (message.startsWith("[SYSTEM]")) append(message, Color.BLUE);
                else if (message.startsWith("[ERROR]")) append(message, Color.RED);
                else if (message.contains("Channel") && message.contains("users")) append(message, new Color(0, 128, 0));
                else append(message, Color.BLACK);
            }

            @Override
            public void onConnectionStatusChanged(boolean connected, String message) {
                append("[STATUS] " + message, Color.GRAY);
                if (connected) {
                    messageField.requestFocusInWindow();
                }
            }
        });

        // Buton Aksiyonları
        connectButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String ip = ipField.getText().trim();
            if (user.isEmpty()) {
                append("[ERROR] Username boş olamaz", Color.RED);
                return;
            }
            if (client.connect(ip, 1234, user)) {
                connectButton.setEnabled(false);
                ipField.setEditable(false);
                usernameField.setEditable(false);
            }
        });

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        joinButton.addActionListener(e -> {
            try { client.changeChannel(Integer.parseInt(channelField.getText())); } catch (Exception ex) {}
        });
        listButton.addActionListener(e -> client.requestChannelList());
        usersButton.addActionListener(e -> client.requestUsers());

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty()) {
            client.sendMessage(msg);
            messageField.setText("");
            messageField.requestFocusInWindow();
        }
    }

    private void append(String msg, Color c) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = chatArea.getStyledDocument();
                Style style = chatArea.addStyle("Style", null);
                StyleConstants.setForeground(style, c);
                doc.insertString(doc.getLength(), msg + "\n", style);
                chatArea.setCaretPosition(doc.getLength());
            } catch (Exception e) {}
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatGUI::new);
    }
}