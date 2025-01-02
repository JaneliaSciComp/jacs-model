package org.janelia.model.access.domain.dao.mongo.mongodbutils;

import java.io.IOException;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class MongoNumberDeserializer extends JsonDeserializer<Number> {

    Pattern FLOATING_POINT_PATTERN = Pattern.compile("^[-+]?[0-9]*\\.[0-9]+([eE][-+]?[0-9]+)?$");

    @Override
    public Number deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode node = jsonParser.readValueAsTree();
        if (node.get("$numberLong") != null) {
            return Long.valueOf(node.get("$numberLong").asText());
        } else {
            String value = node.asText();
            if (FLOATING_POINT_PATTERN.matcher(value).matches()) {
                return Double.valueOf(value);
            } else {
                return Long.valueOf(node.asText());
            }
        }
    }
}
