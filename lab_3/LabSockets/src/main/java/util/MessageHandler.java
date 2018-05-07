package util;

import javafx.application.Platform;
import models.Message;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class MessageHandler implements Runnable {

    private LinkedBlockingQueue<Message> messages;
    private Consumer<Message> onMessage;

    public MessageHandler(LinkedBlockingQueue<Message> messages, Consumer<Message> onMessage) {
        this.messages = messages;
        this.onMessage = onMessage;
    }

    @Override
    public void run() {
        while (true) {
            try {
                final Message message = messages.take();
                Platform.runLater(() -> onMessage.accept(message));
                System.out.println("Message Received: " + message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
