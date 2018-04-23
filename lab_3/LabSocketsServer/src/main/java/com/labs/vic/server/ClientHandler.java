package com.labs.vic.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.labs.vic.models.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

class ClientHandler {
    private PrintWriter out;
    private BufferedReader in;
    private LinkedBlockingQueue<Message> messages;
    private Socket socket;

    ClientHandler(Socket socket, LinkedBlockingQueue<Message> messages) throws IOException {
        this.socket = socket;
        this.messages = messages;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Thread reader = new Thread(() -> {
            try {
                while (true) {
                    String requestString = in.readLine();
                    if (requestString != null) {
                        Message request = EchoServer.jsonObjectMapper.readValue(requestString, Message.class);
                        messages.put(request);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        reader.setDaemon(true);
        reader.start();
    }

    public void sendMessage(Message message) {
        try {
            out.println(EchoServer.jsonObjectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
