package models;

import external.diff_match_patch;
import lombok.Data;

import java.util.Date;
import java.util.LinkedList;

public @Data
class Message {
    private LinkedList<diff_match_patch.Patch> patches;
    private String author;
    private Date date;
    private int stateId; // the state of file when changes were done

    public Message(LinkedList<diff_match_patch.Patch> patches, String author, int stateId) {
        this.patches = patches;
        this.author = author;
        this.stateId = stateId;
        this.date = new Date();
    }

    public Message() {
    }
}