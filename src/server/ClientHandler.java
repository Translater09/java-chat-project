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

            // Sadece mevcut kanaldaki DİĞERLERİNE haber ver [cite: 5, 8]
            sendToOthers("[SYSTEM][" + channel + "] " + username + " joined");

            String message;

            while ((message = in.readLine()) != null) {

                // 🔥 /list KOMUTU [cite: 11, 12]
                if (message.equals("/list")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("[SYSTEM] Channels:\n");

                    for (int i = 1; i <= 100; i++) {
                        int count = 0;

                        if (Server.channels.containsKey(i)) {
                            count = Server.channels.get(i).size();
                        }

                        sb.append("Channel ").append(i)
                                .append(" (").append(count).append(" users)");

                        if (count == 0) {
                            sb.append(" [empty]");
                        }

                        sb.append("\n");
                    }

                    out.println(sb.toString());
                    continue;
                }

                // 🔥 /users KOMUTU [cite: 24]
                if (message.equals("/users")) {
                    List<ClientHandler> currentUsers = Server.channels.get(channel);

                    StringBuilder sb = new StringBuilder();
                    sb.append("[SYSTEM] Users in channel ").append(channel).append(":\n");

                    if (currentUsers != null) {
                        for (ClientHandler client : currentUsers) {
                            if (client.isReady) {
                                sb.append("- ").append(client.username).append("\n");
                            }
                        }
                    }

                    out.println(sb.toString());
                    continue;
                }

                // 🔥 /join (HATA DÜZELTMESİ BURADA) [cite: 6, 21, 23]
                if (message.startsWith("/join")) {
                    try {
                        int newChannel = Integer.parseInt(message.split(" ")[1]);
                        
                        // Zaten o kanaldaysa veya geçersizse işlem yapma 
                        if (newChannel < 1 || newChannel > 100 || newChannel == channel) {
                            continue;
                        }

                        int oldChannel = channel;

                        // 1. ESKİ KANALA BİLDİR: Sadece ordakiler duysun
                        sendToOthers("[SYSTEM][" + oldChannel + "] " + username + " left");

                        if (Server.channels.get(oldChannel) != null) {
                            Server.channels.get(oldChannel).remove(this);
                        }

                        channel = newChannel;
                        Server.channels.putIfAbsent(channel, new java.util.ArrayList<>());
                        Server.channels.get(channel).add(this);

                        System.out.println("[INFO] " + username + " switching from " + oldChannel + " to " + newChannel);

                        // 2. YENİ KANALA BİLDİR: Sadece yenidekiler duysun
                        sendToOthers("[SYSTEM][" + channel + "] " + username + " joined");

                        // 3. KULLANICIYA BİLGİ VER
                        out.println("[SYSTEM] Switched to channel " + channel);
                    } catch (Exception e) {
                        out.println("[SYSTEM] Invalid channel format");
                    }
                    continue;
                }

                // normal mesaj [cite: 22]
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
            // SADECE AYNI KANALDAKİ DİĞERLERİNE GİDER 
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