package com.labs.vic.server;

import com.labs.vic.models.Message;
import com.labs.vic.util.MessageHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketServer {

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

    public SocketServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clients = new ArrayList<>();
        messages = new LinkedBlockingQueue<>();

        Thread messageHandling = new Thread(new MessageHandler(messages, this::broadcastMessage));

        messageHandling.setDaemon(true);
        messageHandling.start();
    }

    private void broadcastMessage(Message message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
