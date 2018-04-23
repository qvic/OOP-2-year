package socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

//public class EchoClient {
//    private Socket clientSocket;
//    private PrintWriter out;
//    private BufferedReader in;
//
//    public EchoClient(String ip, int port) throws IOException {
//        clientSocket = new Socket(ip, port);
//        out = new PrintWriter(clientSocket.getOutputStream(), true);
//        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//    }
//
//    public Message sendAndGet(Message message) throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//
//        out.println(mapper.writeValueAsString(message));
//
//        return mapper.readValue(in.readLine(), Message.class);
//    }
//
//    public void close() throws IOException {
//        in.close();
//        out.close();
//        clientSocket.close();
//    }
//}

public class EchoClient {

    public static final ObjectMapper jsonObjectMapper = new ObjectMapper();
    private ConnectionToServer server;
    private LinkedBlockingQueue<Message> messages;
    private Socket socket;

    private class ConnectionToServer {

        private BufferedReader in;
        private PrintWriter out;
        private Socket socket;

        ConnectionToServer(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Thread reader = new Thread(() -> {
                try {
                    while (true) {
                        String requestString = in.readLine();
                        if (requestString != null) {
                            Message request = jsonObjectMapper.readValue(requestString, Message.class);
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

        private void sendMessage(Message message) {
            try {
                out.println(jsonObjectMapper.writeValueAsString(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public EchoClient(String ip, int port, Consumer<Message> onReceive) throws IOException {
        socket = new Socket(ip, port);
        messages = new LinkedBlockingQueue<>();
        server = new ConnectionToServer(socket);

        Thread messageHandling = new Thread(() -> {
            while (true) {
                try {
                    Message message = messages.take();
                    onReceive.accept(message);
                    System.out.println("Message Received: " + message);
                } catch (InterruptedException e) {
                }
            }
        });

        messageHandling.setDaemon(true);
        messageHandling.start();
    }

    public void send(Message message) {
        server.sendMessage(message);
    }
}
