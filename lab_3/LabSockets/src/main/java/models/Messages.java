package models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import external.diff_match_patch;

import java.io.IOException;

public class Messages {

    private static final ObjectMapper objectMapper = getObjectMapper();

    private static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(diff_match_patch.Diff.class, new DiffDeserializer());
        mapper.registerModule(module);

        return mapper;
    }

    public static JsonNode toJsonNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Message toMessage(JsonNode node) {
        try {
            return objectMapper.treeToValue(node, Message.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static CursorChange toCursorChange(JsonNode node) {
        try {
            return objectMapper.treeToValue(node, CursorChange.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toJson(String type, Object message) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("type", type).set("body", objectMapper.valueToTree(message));

            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

