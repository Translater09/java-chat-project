package client;

import java.io.*;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 1234);

            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Server'dan gelen mesajları dinle
            new Thread(() -> {
                String msg;
                try {
                    while ((msg = in.readLine()) != null) {
                        System.out.print("\r");      // satır başına dön
                        if (msg.startsWith("[SYSTEM]")) {
                            System.out.println("\u001B[31m" + msg + "\u001B[0m"); // kırmızı
                        } else {
                            System.out.println(msg);
                        }   // mesajı yaz
                        System.out.print("> ");     // input tekrar
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected");
                }
            }).start();

            // Kullanıcı input
            String input;

            while ((input = keyboard.readLine()) != null) {
                out.println(input);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}