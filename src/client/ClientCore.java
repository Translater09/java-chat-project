package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientCore {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private String username;
    private int currentChannel = 16;
    private boolean connected = false;

    private MessageListener messageListener;

    public interface MessageListener {
        void onMessageReceived(String message);
        void onConnectionStatusChanged(boolean connected, String message);
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public boolean connect(String host, int port, String username) {
        try {
            this.username = username;

            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            
            String firstMessage = in.readLine();
            if (firstMessage != null) {
                notifyMessage(firstMessage);
            }

            // send username
            out.println(username);

            connected = true;
            notifyStatus(true, "Connected to server");

            listenMessages();

            return true;

        } catch (IOException e) {
            notifyStatus(false, "Connection failed: " + e.getMessage());
            return false;
        }
    }

    private void listenMessages() {
        Thread listenerThread = new Thread(() -> {
            try {
                String message;
                while (connected && (message = in.readLine()) != null) {
                    notifyMessage(message);

                    
                    if (message.startsWith("[SYSTEM] Switched to channel ")) {
                        try {
                            String number = message.replace("[SYSTEM] Switched to channel ", "").trim();
                            currentChannel = Integer.parseInt(number);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    notifyStatus(false, "Disconnected: " + e.getMessage());
                }
            } finally {
                disconnect();
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void sendMessage(String message) {
        if (!connected || out == null) {
            notifyMessage("[SYSTEM] Not connected");
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            return;
        }

        out.println(message);
    }

    public void changeChannel(int newChannel) {
        if (newChannel < 1 || newChannel > 100) {
            notifyMessage("[SYSTEM] Channel must be between 1 and 100");
            return;
        }

        sendMessage("/join " + newChannel);
    }

    public void requestChannelList() {
        sendMessage("/list");
    }

    public void requestUsers() {
        sendMessage("/users");
    }

    public void disconnect() {
        if (!connected && socket == null) {
            return;
        }

        connected = false;

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }

        notifyStatus(false, "Disconnected");
    }

    public boolean isConnected() {
        return connected;
    }

    public String getUsername() {
        return username;
    }

    public int getCurrentChannel() {
        return currentChannel;
    }

    private void notifyMessage(String message) {
        if (messageListener != null) {
            messageListener.onMessageReceived(message);
        } else {
            System.out.println(message);
        }
    }

    private void notifyStatus(boolean connected, String message) {
        if (messageListener != null) {
            messageListener.onConnectionStatusChanged(connected, message);
        } else {
            System.out.println(message);
        }
    }

    // temporary main method for terminal based testing
    public static void main(String[] args) {
        ClientCore client = new ClientCore();

        client.setMessageListener(new MessageListener() {
            @Override
            public void onMessageReceived(String message) {
                System.out.println(message);
            }

            @Override
            public void onConnectionStatusChanged(boolean connected, String message) {
                System.out.println(message);
            }
        });

        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.print("Username: ");
            String username = keyboard.readLine();

            boolean ok = client.connect("localhost", 1234, username);
            if (!ok) {
                return;
            }

            System.out.println("Commands:");
            System.out.println("/join <channel>");
            System.out.println("/list");
            System.out.println("/users");
            System.out.println("/quit");

            String input;
            while ((input = keyboard.readLine()) != null) {
                if (input.equalsIgnoreCase("/quit")) {
                    client.disconnect();
                    break;
                } else if (input.startsWith("/join ")) {
                    try {
                        int channel = Integer.parseInt(input.split(" ")[1]);
                        client.changeChannel(channel);
                    } catch (Exception e) {
                        System.out.println("[SYSTEM] Invalid channel");
                    }
                } else if (input.equals("/list")) {
                    client.requestChannelList();
                } else if (input.equals("/users")) {
                    client.requestUsers();
                } else {
                    client.sendMessage(input);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}