package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.dao.EntityUtils;
import org.janelia.model.util.TimebasedIdentifierGenerator;

import java.util.List;

/**
 * Abstract Mongo DAO.
 */
public abstract class AbstractMongoDao {

    final MongoDatabase mongoDatabase;

    AbstractMongoDao(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    Long createNewId() {
        return TimebasedIdentifierGenerator.generateIdList(1).get(0);
    }

    List<Long> createNewIds(int size) {
        return TimebasedIdentifierGenerator.generateIdList(size);
    }

    <T> MongoCollection<T> getEntityCollection(Class<T> entityClass) {
        String entityCollectionName = EntityUtils.getPersistenceInfo(entityClass).collectionName();
        return mongoDatabase.getCollection(entityCollectionName, entityClass);
    }

}
