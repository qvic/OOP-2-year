package util;

import models.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageReader implements Runnable {

    private BufferedReader in;
    private LinkedBlockingQueue<Message> messages;

    public MessageReader(BufferedReader in, LinkedBlockingQueue<Message> messages) {
        this.in = in;
        this.messages = messages;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String requestJson = in.readLine();
                if (requestJson != null) {
                    Message request = Objects.requireNonNull(
                            Messages.jsonToMessage(requestJson),
                            "Cannot process given JSON: " + requestJson
                    );
                    messages.put(request);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
