package org.janelia.model.access.domain.dao.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.janelia.model.access.cdi.DaoObjectMapper;

import javax.inject.Inject;
import java.util.stream.Collectors;

/**
 * Entity update helper for a mongo store.
 */
class DomainUpdateMongoHelper {

    private final ObjectMapper objectMapper;

    @Inject
    DomainUpdateMongoHelper(@DaoObjectMapper ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    <T> Bson getEntityUpdates(T entity) {
        try {
            String jsonEntity = objectMapper.writeValueAsString(entity);
            Document bsonEntity = Document.parse(jsonEntity);
            return Updates.combine(bsonEntity.entrySet().stream().map(e -> {
                Object value = e.getValue();
                if (value == null) {
                    return Updates.unset(e.getKey());
                } else {
                    return Updates.set(e.getKey(), e.getValue());
                }
            }).collect(Collectors.toList()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

}
