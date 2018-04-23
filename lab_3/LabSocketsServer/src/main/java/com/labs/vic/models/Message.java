package com.labs.vic.models;

public class Message {
    private String text;

    public Message(String text) {
        this.text = text;
    }

    public Message() {}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "<Message> [" + text + "]";
    }
}
