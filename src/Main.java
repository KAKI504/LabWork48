import Server.VotingServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            new VotingServer("localhost", 9889).start();
            System.out.println("Сервер запущен на http://localhost:9889");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}