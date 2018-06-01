package org.janelia.model.access.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.janelia.model.util.TimebasedIdentifierGenerator;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jongo.marshall.jackson.JacksonMapper;
import org.jongo.marshall.jackson.configuration.MapperModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class BaseDAO {

    private static final Logger LOG = LoggerFactory.getLogger(BaseDAO.class);

    protected String databaseName;
    protected MongoClient mongoClient;
    protected Jongo jongo;

    public BaseDAO(MongoClient mongoClient, String databaseName) {
        this.mongoClient = mongoClient;
        this.databaseName = databaseName;

        JacksonMapper.Builder builder = new JacksonMapper.Builder()
                .enable(MapperFeature.AUTO_DETECT_GETTERS)
                .enable(MapperFeature.AUTO_DETECT_SETTERS)
                .addModifier(new MapperModifier() {
                    @Override
                    public void modify(ObjectMapper mapper) {
                        // So that Map<String,Object> serializes DomainObjects with proper class metadata
                        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "class");
                    }
                });

        this.jongo = new Jongo(mongoClient.getDB(databaseName), builder.build());
    }

    public com.mongodb.client.MongoCollection<Document> getNativeCollection(String collectionName) {
        MongoDatabase db = mongoClient.getDatabase(databaseName);
        return db.getCollection(collectionName);
    }

    public final MongoCollection getCollectionByClass(Class<?> domainClass) {
        if (domainClass == null) {
            throw new IllegalArgumentException("domainClass argument may not be null");
        }
        String collectionName = DomainUtils.getCollectionName(domainClass);
        return getCollectionByName(collectionName);
    }

    public MongoCollection getCollectionByName(String collectionName) {
        if (collectionName == null) {
            throw new IllegalArgumentException("collectionName argument may not be null");
        }
        return jongo.getCollection(collectionName);
    }

    public MongoClient getMongo() {
        return mongoClient;
    }

    public Jongo getJongo() {
        return jongo;
    }

    public Long getNewId() {
        return TimebasedIdentifierGenerator.generateIdList(1).get(0);
    }

    /**
     * Create a list of the result set in iteration order.
     */
    public <T> List<T> toList(MongoCursor<? extends T> cursor) {
        List<T> list = new ArrayList<>();
        for (T item : cursor) {
            list.add(item);
        }
        return list;
    }
}
