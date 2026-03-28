package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    public static Map<Integer, List<ClientHandler>> channels = new HashMap<>();
    public static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Server started...");

            channels.putIfAbsent(16, new ArrayList<>());

            while (true) {
                Socket socket = serverSocket.accept();

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