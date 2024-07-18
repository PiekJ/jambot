package dev.joopie.jambot.api.youtube;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class IdDeserializer extends JsonDeserializer<SearchResponse.Item.Id> {

    @Override
    public SearchResponse.Item.Id deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        SearchResponse.Item.Id id = new SearchResponse.Item.Id();
        if (node.isObject()) {
            id.setKind(node.get("kind").asText());
            id.setVideoId(node.get("videoId").asText());
        } else if (node.isTextual()) {
            id.setVideoId(node.asText());
        }
        return id;
    }
}
