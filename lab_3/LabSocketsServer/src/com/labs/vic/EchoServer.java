package com.labs.vic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer implements AutoCloseable {

    private ServerSocket serverSocket;

    private static class ClientHandler extends Thread {
        private Socket clientSocket;

        ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                String inputLine = in.readLine();
                while (inputLine != null) {
                    if ("exit".equals(inputLine.toLowerCase())) {
                        out.println("bye");
                        break;
                    }
                    System.out.println("Got (port " + clientSocket.getPort() + "): " + inputLine);
                    out.println(inputLine);

                    inputLine = in.readLine();
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

    public void listen(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true) {
            new ClientHandler(serverSocket.accept()).start();
        }
    }

    public void close() throws IOException {
        serverSocket.close();
    }
}
