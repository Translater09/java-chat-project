package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    public static Map<Integer, List<ClientHandler>> channels = new ConcurrentHashMap<>();
    public static List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static final int MAX_CLIENTS = 50;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Server started on port 1234...");

            for (int i = 1; i <= 100; i++) {
                channels.put(i, new CopyOnWriteArrayList<>());
            }

            while (true) {
                Socket socket = serverSocket.accept();

                if (clients.size() >= MAX_CLIENTS) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("ERROR: Server is full. Maximum 50 users allowed.");
                    socket.close();
                    continue;
                }

                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                channels.get(16).add(handler);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}