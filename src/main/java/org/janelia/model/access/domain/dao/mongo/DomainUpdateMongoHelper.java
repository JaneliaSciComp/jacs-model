package org.janelia.model.access.domain.dao.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.DomainUtils;
import org.janelia.model.access.domain.dao.DomainObjectDao;
import org.janelia.model.cdi.DaoObjectMapper;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
