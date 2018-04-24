package com.labs.vic;

import com.labs.vic.server.SocketServer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            SocketServer socketServer = new SocketServer(8080);
            System.out.println("Connection established");
            socketServer.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
