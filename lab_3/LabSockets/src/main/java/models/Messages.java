package models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import external.diff_match_patch;

import java.io.IOException;

public class Messages {

    private static final String TYPE = "type";
    private static final String BODY = "body";

    public enum Type {
        TEXT, CURSOR

    }

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
            return objectMapper.treeToValue(node.get(BODY), Message.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static CursorChange toCursorChange(JsonNode node) {
        try {
            return objectMapper.treeToValue(node.get(BODY), CursorChange.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toJson(Type type, Object message) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put(TYPE, type.toString()).set(BODY, objectMapper.valueToTree(message));

            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Type getType(JsonNode node) {
        return Type.valueOf(node.get(TYPE).asText());
    }
}

