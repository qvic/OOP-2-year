package server;

import models.Message;

import java.util.ArrayList;

public class States {
    private ArrayList<Message> textMessages;

    public States() {
        textMessages = new ArrayList<>();
    }

    public void append(Message message) {
        message.setStateId(textMessages.size());
        textMessages.add(message);
    }

    public Message getLast() {
        if (!textMessages.isEmpty()) {
            return textMessages.get(textMessages.size() - 1);
        }
        throw new NullPointerException("No available states");
    }
}
