package server;

import com.fasterxml.jackson.databind.JsonNode;
import models.CursorChange;
import models.Message;
import models.Messages;
import util.MessageHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Currency;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketServer {

    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> clients;
    private LinkedBlockingQueue<JsonNode> messages;

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

    private void onMessage(JsonNode message) {
        String type = message.get("type").asText();
        if (type.equals("text")) {

            states.append(Messages.toMessage(message.get("body")));
            broadcast(states.getLast());

        } else if (type.equals("cursor")) {

            sendOthers(Messages.toCursorChange(message.get("body")));
        }
    }

    private void broadcast(Message message) {
        for (ClientHandler client : clients) {
            if (!client.getAddress().equals(message.getAuthor())) {
                client.sendMessage(message);
            }
        }
    }

    private void sendOthers(CursorChange message) {
        for (ClientHandler client : clients) {
            if (!client.getAddress().equals(message.getAuthor())) {
                client.sendMessage(message);
            }
        }
    }
}
