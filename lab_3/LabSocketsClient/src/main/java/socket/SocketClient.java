package socket;

import models.Message;
import util.MessageHandler;
import util.MessageReader;
import util.Messages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class SocketClient {

    private PrintWriter out;
    private BufferedReader in;

    public SocketClient(String ip, int port, Consumer<Message> onMessage) throws IOException {
        Socket socket = new Socket(ip, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<>();

        Thread messageHandler = new Thread(new MessageHandler(messages, onMessage));
        Thread messageReader = new Thread(new MessageReader(in, messages));

        messageHandler.setDaemon(true);
        messageHandler.start();

        messageReader.setDaemon(true);
        messageReader.start();
    }

    public void send(Message message) {
        out.println(
                Messages.messageToJson(Objects.requireNonNull(message, "Cannot send 'null' message"))
        );
    }
}
