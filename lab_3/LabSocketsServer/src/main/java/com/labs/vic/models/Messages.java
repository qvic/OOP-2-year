package com.labs.vic.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Messages {
    public enum Type {
        COMMAND, TEXT
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Message toMessage(String json) {
        try {
            return objectMapper.readValue(json, Message.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toJson(Message message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

