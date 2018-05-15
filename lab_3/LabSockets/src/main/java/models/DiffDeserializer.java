package models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import external.diff_match_patch;

import java.io.IOException;

public class DiffDeserializer extends StdDeserializer<diff_match_patch.Diff> {

    public DiffDeserializer() {
        this(null);
    }

    public DiffDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public diff_match_patch.Diff deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        diff_match_patch.Operation operation = diff_match_patch.Operation.valueOf(node.get("operation").asText());
        String text = node.get("text").asText();

        return new diff_match_patch.Diff(operation, text);
    }
}
