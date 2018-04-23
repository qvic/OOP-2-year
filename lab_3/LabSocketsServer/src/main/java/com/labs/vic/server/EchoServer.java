package com.labs.vic.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labs.vic.models.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class EchoServer {

    public static final ObjectMapper jsonObjectMapper = new ObjectMapper();
    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> clients;
    private LinkedBlockingQueue<Message> messages;

    public void listen() throws IOException {
        while (true) {
            clients.add(new ClientHandler(serverSocket.accept(), messages));
        }
    }

    public void close() throws IOException {
        serverSocket.close();
    }

    public EchoServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clients = new ArrayList<>();
        messages = new LinkedBlockingQueue<>();

        Thread messageHandling = new Thread(() -> {
            while (true) {
                try {
                    Message message = messages.take();
                    broadcastMessage(message);
//                    broadcastMessage(new Message("Poshli nahuy"));

                    System.out.println("Message Received: " + message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        messageHandling.setDaemon(true);
        messageHandling.start();
    }

    private void broadcastMessage(Message message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
