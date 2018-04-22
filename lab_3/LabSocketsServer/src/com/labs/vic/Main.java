package com.labs.vic;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
	 try (EchoServer echoServer = new EchoServer()) {
        echoServer.start(8080);
     } catch (IOException e) {
         e.printStackTrace();
     }
    }
}
