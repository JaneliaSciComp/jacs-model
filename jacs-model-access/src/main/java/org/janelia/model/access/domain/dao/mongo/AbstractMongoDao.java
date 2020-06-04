package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.dao.EntityUtils;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract Mongo DAO.
 */
abstract class AbstractMongoDao {

    final TimebasedIdentifierGenerator idGenerator;
    final MongoDatabase mongoDatabase;

    AbstractMongoDao(MongoDatabase mongoDatabase, TimebasedIdentifierGenerator idGenerator) {
        this.mongoDatabase = mongoDatabase;
        this.idGenerator = idGenerator;
    }

    Long createNewId() {
        return idGenerator.generateIdList(1).get(0);
    }

    List<Long> createNewIds(int size) {
        return idGenerator.generateIdList(size);
    }

    <T> MongoCollection<T> getEntityCollection(Class<T> entityClass) {
        String entityCollectionName = EntityUtils.getPersistenceInfo(entityClass).collectionName();
        return mongoDatabase.getCollection(entityCollectionName, entityClass);
    }
}
