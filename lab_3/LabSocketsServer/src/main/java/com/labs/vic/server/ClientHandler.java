package com.labs.vic.server;

import com.labs.vic.models.Message;
import com.labs.vic.util.Messages;
import com.labs.vic.util.MessageReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

class ClientHandler {
    private PrintWriter out;
    private BufferedReader in;

    ClientHandler(Socket socket, LinkedBlockingQueue<Message> messages) throws IOException {
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Connected " + socket.getRemoteSocketAddress());

        Thread reader = new Thread(new MessageReader(in, messages));
        reader.setDaemon(true);
        reader.start();
    }

    public void sendMessage(Message message) {
        out.println(
                Messages.messageToJson(Objects.requireNonNull(message, "Cannot send 'null' message"))
        );
    }
}
