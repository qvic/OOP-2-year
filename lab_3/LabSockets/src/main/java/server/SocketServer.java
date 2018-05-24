package server;

import com.fasterxml.jackson.databind.JsonNode;
import models.CursorChange;
import models.Message;
import models.Messages;
import util.MessageHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
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
                    ClientHandler client = new ClientHandler(serverSocket.accept(), messages);
                    clients.add(client);
                    onConnect(client);
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

    private void onConnect(ClientHandler clientHandler) {
        for (Message message: states) {
            clientHandler.send(message);
        }
    }

    private void onMessage(JsonNode node) {
        Messages.Type type = Messages.getType(node);

        if (type == Messages.Type.TEXT) {

            Message message = Messages.toMessage(node);
            assert message != null;
            states.append(message);
            broadcast(states.getLast());

        } else if (type == Messages.Type.CURSOR) {

            CursorChange cursorChange = Messages.toCursorChange(node);
            assert cursorChange != null;
            sendOthers(cursorChange);
        }
    }

    private void broadcast(Message message) {
        for (ClientHandler client : clients) {
//            if (!client.getAddress().equals(message.getAuthor())) {
                client.send(message);
//            }
        }
    }

    private void sendOthers(CursorChange message) {
        for (ClientHandler client : clients) {
            if (!client.getAddress().equals(message.getAuthor())) {
                client.send(message);
            }
        }
    }
}
