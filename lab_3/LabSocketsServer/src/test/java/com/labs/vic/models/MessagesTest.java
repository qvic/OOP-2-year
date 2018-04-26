package com.labs.vic.models;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class MessagesTest {

    @Test
    public void toMessage() {
        String json = "{\"type\":\"TEXT\",\"body\":\"TestBody\",\"author\":\"TestAuthor\",\"date\":1234}";

        Message expected = new Message(Messages.Type.TEXT, "TestBody", "TestAuthor", new Date(1234));
        assertEquals(expected, Messages.toMessage(json));
    }

    @Test
    public void toJson() {
        Message message = new Message(Messages.Type.TEXT, "TestBody", "TestAuthor", new Date(1234));

        String expected = "{\"type\":\"TEXT\",\"body\":\"TestBody\",\"author\":\"TestAuthor\",\"date\":1234}";
        assertEquals(expected, Messages.toJson(message));
    }
}