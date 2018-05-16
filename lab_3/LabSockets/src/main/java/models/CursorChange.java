package models;

import lombok.Data;

public @Data
class CursorChange {
    private String author;
    private int position;

    public CursorChange(int position, String author) {
        this.author = author;
        this.position = position;
    }

    public CursorChange() {

    }
}
