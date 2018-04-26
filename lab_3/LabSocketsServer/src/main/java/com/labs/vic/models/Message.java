package com.labs.vic.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

public @Data
class Message {

    private Messages.Type type;
    private String body;
    private String author;
    private Date date;

    public Message(Messages.Type type, String body, String author, Date date) {
        this.type = type;
        this.body = body;
        this.author = author;
        this.date = date;
    }

    public Message() {
    }

    @JsonIgnore
    public boolean isCommand() {
        return type.equals(Messages.Type.COMMAND);
    }
}

