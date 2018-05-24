package models;

import lombok.Data;

import java.util.Date;

public @Data
class Message {
    private String body;
    private String author;
    private Date date;

    public Message(String body, String author) {
        this.body = body;
        this.author = author;
        this.date = new Date();
    }

    public Message() {
    }
}