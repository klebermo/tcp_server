package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import model.Message;
import utils.JAXBUtils;

public class TCPServer {
    private static final int portNumber = 5555;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(portNumber);
        System.out.println("Server is listening on port " + portNumber);

        Thread clientHandler = new Thread(() -> {
            while(!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                    new Thread(() -> messageHandler(clientSocket)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        clientHandler.start();        

        Thread keyboardThread = new Thread(() -> {
            try (BufferedReader keyboardReader = new BufferedReader(new InputStreamReader(System.in))) {
                while(true) {
                    String input = keyboardReader.readLine();
                    if(input.equalsIgnoreCase("q")) {
                        serverSocket.close();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        keyboardThread.start();
    }

    private static void messageHandler(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String inputLine;

            while((inputLine = reader.readLine()) != null) {
                Message receivedMessage = JAXBUtils.unmarshal(inputLine, Message.class);
                System.out.println("[" + receivedMessage.getUser() + "] " + receivedMessage.getContent() + "\n");
                clientSocket.getOutputStream().flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

