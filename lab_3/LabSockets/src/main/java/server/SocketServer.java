package server;

import models.Message;
import util.MessageHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketServer {

    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> clients;
    private LinkedBlockingQueue<Message> messages;

    private States states;

    public void startListening() {
        Thread listener = new Thread(() -> {
            while (true) {
                try {
                    clients.add(new ClientHandler(serverSocket.accept(), messages));
                } catch (IOException ignored) {
                }
            }
        });
        listener.start();
    }

    public void close() throws IOException {
        serverSocket.close();
    }

    public SocketServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clients = new ArrayList<>();
        messages = new LinkedBlockingQueue<>();
        states = new States();

        Thread messageHandling = new Thread(new MessageHandler(messages, this::onMessage));

        messageHandling.setDaemon(true);
        messageHandling.start();
    }

    private void onMessage(Message message) {
        states.append(message);

        for (ClientHandler client : clients) {
            client.sendMessage(states.getLast());
//            TODO: say everyone about cursor positions
        }
    }
}
