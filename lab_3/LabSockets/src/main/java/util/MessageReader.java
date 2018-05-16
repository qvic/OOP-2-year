package util;

import com.fasterxml.jackson.databind.JsonNode;
import models.Message;
import models.Messages;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageReader implements Runnable {

    private BufferedReader in;
    private LinkedBlockingQueue<JsonNode> messages;

    public MessageReader(BufferedReader in, LinkedBlockingQueue<JsonNode> messages) {
        this.in = in;
        this.messages = messages;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String requestJson = in.readLine();
                if (requestJson != null) {
                    JsonNode request = Objects.requireNonNull(
                            Messages.toJsonNode(requestJson),
                            "Cannot process given JSON: " + requestJson
                    );
//                    Thread.sleep(1000); // fake ping
                    System.out.println("Message Received: " + request);
                    messages.put(request);
                }
            }
        } catch (IOException ignored) {
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
