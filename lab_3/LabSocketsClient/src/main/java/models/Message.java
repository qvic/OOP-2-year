package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import util.Messages;

import java.util.Date;

public class Message {

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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @JsonIgnore
    public boolean isCommand() {
        return type.equals(Messages.Type.COMMAND);
    }

    @Override
    public String toString() {
        return "<" + type + " Message from " + author + "> [" + body + "]";
    }


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Messages.Type getType() {
        return type;
    }

    public void setType(Messages.Type type) {
        this.type = type;
    }
}
