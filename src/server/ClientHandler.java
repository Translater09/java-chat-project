package server;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler extends Thread {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private int channel = 16;
    private String username;
    private boolean isReady = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            out.println("[SYSTEM] Enter username:");
            username = in.readLine();

            isReady = true;
            List<ClientHandler> users = Server.channels.get(channel);
            for (ClientHandler client : users) {
                if (client != this && client.isReady) {
                    out.println("[SYSTEM][" + channel + "] " + client.username + " already in channel");
                }
            }
            System.out.println("[INFO] " + username + " connected (channel " + channel + ")");

            sendToOthers("[SYSTEM][" + channel + "] " + username + " joined");

            String message;

            while ((message = in.readLine()) != null) {

                if (message.startsWith("/join")) {

                    int newChannel = Integer.parseInt(message.split(" ")[1]);
                    int oldChannel = channel;

                    System.out.println("[INFO] " + username +
                            " switching from " + oldChannel + " to " + newChannel);

                    if (Server.channels.get(channel) != null) {
                        Server.channels.get(channel).remove(this);
                    }

                    channel = newChannel;

                    Server.channels.putIfAbsent(channel, new java.util.ArrayList<>());
                    Server.channels.get(channel).add(this);

                    sendToOthers("[SYSTEM][" + oldChannel + "] " + username + " left");
                    sendToOthers("[SYSTEM][" + channel + "] " + username + " joined");

                    out.println("[SYSTEM] Switched to channel " + channel);
                    continue;
                }

                sendToChannel("[" + channel + "][" + username + "]: " + message);
            }

        } catch (IOException e) {
            System.out.println("[INFO] " + username + " disconnected");
        } finally {
            cleanup();
        }
    }

    private void sendToChannel(String message) {
        List<ClientHandler> users = Server.channels.get(channel);

        if (users == null) return;

        for (ClientHandler client : users) {
            if (client.isReady) {
                client.out.println(message);
            }
        }
    }

    private void sendToOthers(String message) {
        List<ClientHandler> users = Server.channels.get(channel);

        if (users == null) return;

        for (ClientHandler client : users) {
            if (client != this && client.isReady) {
                client.out.println(message);
            }
        }
    }

    private void cleanup() {
        try {
            if (Server.channels.get(channel) != null) {
                Server.channels.get(channel).remove(this);
            }
            Server.clients.remove(this);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}