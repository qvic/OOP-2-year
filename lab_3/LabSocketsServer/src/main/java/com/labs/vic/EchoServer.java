package com.labs.vic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labs.vic.models.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {

    private ServerSocket serverSocket;

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private static final ObjectMapper objectMapper = new ObjectMapper();

        ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void sendMessage(Message message) {

        }

        public void run() {
            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                String requestString = in.readLine();
                while (requestString != null) {
                    Message request = objectMapper.readValue(requestString, Message.class);

                    if ("exit".equals(request.getText().toLowerCase())) {

                        Message response = new Message("Bye");
                        out.println(objectMapper.writeValueAsString(response));

                        break;
                    }

                    System.out.println("Got (port " + clientSocket.getPort() + "): " + request.toString());

                    try {
                        // adding fake ping
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Message response = new Message(request.getText());
                    out.println(objectMapper.writeValueAsString(response));

                    requestString = in.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    clientSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }
    }

    public void listen() throws IOException {
        while (true) {
            new ClientHandler(serverSocket.accept()).start();
        }
    }

    public void close() throws IOException {
        serverSocket.close();
    }

    public EchoServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }
}
