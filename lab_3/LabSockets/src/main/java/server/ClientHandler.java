package server;

import com.fasterxml.jackson.databind.JsonNode;
import models.CursorChange;
import models.Message;
import models.Messages;
import util.MessageReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

class ClientHandler {
    private PrintWriter out;
    private Socket socket;
    private int currentCursor;

    ClientHandler(Socket socket, LinkedBlockingQueue<JsonNode> messages) throws IOException {
        this.out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Connected " + socket.getRemoteSocketAddress());
        this.socket = socket;

        Thread reader = new Thread(new MessageReader(in, messages));
        reader.setDaemon(true);
        reader.start();
    }

    public String getAddress() {
        return socket.getRemoteSocketAddress().toString();
    }

    public void sendMessage(Message message) {
        out.println(
                Objects.requireNonNull(Messages.toJson("text", message), "Cannot send 'null' message")
        );
    }

    public void sendMessage(CursorChange message) {
        out.println(
                Objects.requireNonNull(Messages.toJson("cursor", message), "Cannot send 'null' message")
        );
    }

    public int getCursor() {
        return currentCursor;
    }

    public void setCursor(int currentCursor) {
        this.currentCursor = currentCursor;
    }
}
