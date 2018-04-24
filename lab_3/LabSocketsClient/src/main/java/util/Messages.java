package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Message;

import java.io.IOException;

public class Messages {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Message jsonToMessage(String json) {
        try {
            return objectMapper.readValue(json, Message.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String messageToJson(Message message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
