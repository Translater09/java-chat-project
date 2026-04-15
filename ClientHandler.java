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
            if (users != null) {
                for (ClientHandler client : users) {
                    if (client != this && client.isReady) {
                        out.println("[SYSTEM][" + channel + "] " + client.username + " already in channel");
                    }
                }
            }

            System.out.println("[INFO] " + username + " connected (channel " + channel + ")");

            sendToOthers("[SYSTEM][" + channel + "] " + username + " joined");

            String message;

            while ((message = in.readLine()) != null) {

                // 🔹 LIST CHANNELS
                if (message.equals("/list")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("[SYSTEM] Channels:\n");

                    for (int i = 1; i <= 100; i++) {
                        int count = Server.channels.get(i).size();

                        sb.append("Channel ").append(i)
                          .append(" (").append(count).append(" users)");

                        if (count == 0) sb.append(" [empty]");
                        sb.append("\n");
                    }

                    out.println(sb.toString());
                    continue;
                }

                
                if (message.equals("/users")) {

                    StringBuilder sb = new StringBuilder();

                    if (channel == 16) {
                        sb.append("[SYSTEM] ALL USERS:\n");

                        for (ClientHandler client : Server.clients) {
                            if (client.isReady) {
                                sb.append("- ").append(client.username)
                                  .append(" (ch ").append(client.channel).append(")\n");
                            }
                        }

                    } else {
                        List<ClientHandler> currentUsers = Server.channels.get(channel);

                        sb.append("[SYSTEM] Users in channel ").append(channel).append(":\n");

                        if (currentUsers != null) {
                            for (ClientHandler client : currentUsers) {
                                if (client.isReady) {
                                    sb.append("- ").append(client.username).append("\n");
                                }
                            }
                        }
                    }

                    out.println(sb.toString());
                    continue;
                }

                // 🔹 JOIN CHANNEL
                if (message.startsWith("/join")) {
                    try {
                        int newChannel = Integer.parseInt(message.split(" ")[1]);

                        if (newChannel < 1 || newChannel > 100 || newChannel == channel)
                            continue;

                        int oldChannel = channel;

                        sendToOthers("[SYSTEM][" + oldChannel + "] " + username + " left");
                        Server.channels.get(oldChannel).remove(this);

                        channel = newChannel;
                        Server.channels.get(channel).add(this);

                        sendToOthers("[SYSTEM][" + channel + "] " + username + " joined");

                        out.println("[SYSTEM] Switched to channel " + channel);

                    } catch (Exception e) {
                        out.println("[SYSTEM] Invalid channel format");
                    }
                    continue;
                }

                
                sendToChannelAndMonitor("[" + channel + "][" + username + "]: " + message);
            }

        } catch (IOException e) {
            System.out.println("[INFO] " + username + " disconnected");
        } finally {
            cleanup();
        }
    }

    // 🔥 EN ÖNEMLİ KISIM (MADDE 9)
    private void sendToChannelAndMonitor(String message) {

        // NORMAL CHANNEL
        List<ClientHandler> users = Server.channels.get(channel);
        if (users != null) {
            for (ClientHandler client : users) {
                if (client.isReady) {
                    client.out.println(message);
                }
            }
        }

        // CHANNEL 16 = GLOBAL MONITOR
        if (channel != 16) {
            List<ClientHandler> monitorUsers = Server.channels.get(16);

            if (monitorUsers != null) {
                for (ClientHandler client : monitorUsers) {
                    if (client.isReady) {
                        client.out.println("[MONITOR] " + message);
                    }
                }
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
            Server.channels.get(channel).remove(this);
            Server.clients.remove(this);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}