package server;

import models.Message;

import java.util.ArrayList;
import java.util.Iterator;

public class States implements Iterable<Message> {
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

    @Override
    public Iterator<Message> iterator() {
        return textMessages.iterator();
    }
}
