package com.labs.vic;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            EchoServer echoServer = new EchoServer(8080);
            System.out.println("Connection established");
            echoServer.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
