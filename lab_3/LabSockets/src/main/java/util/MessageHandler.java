package util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class MessageHandler implements Runnable {

    private LinkedBlockingQueue<JsonNode> messages;
    private Consumer<JsonNode> onMessage;

    public MessageHandler(LinkedBlockingQueue<JsonNode> messages, Consumer<JsonNode> onMessage) {
        this.messages = messages;
        this.onMessage = onMessage;
    }

    @Override
    public void run() {
        while (true) {
            try {
                JsonNode message = messages.take();
                onMessage.accept(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
