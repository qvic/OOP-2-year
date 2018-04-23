package socket;

import models.Message;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class EchoClient {

    private Connection connection;
    private LinkedBlockingQueue<Message> messages;

    public EchoClient(String ip, int port, Consumer<Message> onReceive) throws IOException {
        Socket socket = new Socket(ip, port);
        this.messages = new LinkedBlockingQueue<>();
        this.connection = new Connection(socket, messages);

        Thread messageHandling = new Thread(() -> {
            while (true) {
                try {
                    Message message = messages.take();
                    onReceive.accept(message);
                    System.out.println("Message Received: " + message);
                } catch (InterruptedException ignored) {}
            }
        });

        messageHandling.setDaemon(true);
        messageHandling.start();
    }

    public void send(Message message) {
        connection.sendMessage(message);
    }
}
