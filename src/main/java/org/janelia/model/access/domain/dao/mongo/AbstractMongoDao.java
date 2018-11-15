package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.dao.DaoUpdateResult;
import org.janelia.model.access.domain.dao.EntityFieldValueHandler;
import org.janelia.model.access.domain.dao.EntityUtils;
import org.janelia.model.access.domain.dao.ReadWriteDao;
import org.janelia.model.domain.interfaces.HasIdentifier;
import org.janelia.model.util.TimebasedIdentifierGenerator;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Abstract Mongo DAO.
 *
 * @param <T> type of the element
 */
public abstract class AbstractMongoDao<T extends HasIdentifier> implements ReadWriteDao<T, Long> {

    final MongoCollection<T> mongoCollection;

    AbstractMongoDao(MongoDatabase mongoDatabase) {
        Class<T> entityClass = getEntityType();
        String entityCollectionName = getObjectCollectionName(entityClass);
        mongoCollection = mongoDatabase.getCollection(entityCollectionName, entityClass);
    }

    Long createNewId() {
        return TimebasedIdentifierGenerator.generateIdList(1).get(0);
    }

    List<Long> createNewIds(int size) {
        return TimebasedIdentifierGenerator.generateIdList(size);
    }

    private String getObjectCollectionName(Class<T> entityClass) {
        return EntityUtils.getPersistenceInfo(entityClass).collectionName();
    }

    @Override
    public T findById(Long id) {
        return MongoDaoHelper.findById(id, mongoCollection, getEntityType());
    }

    @Override
    public List<T> findByIds(Collection<Long> ids) {
        return MongoDaoHelper.findByIds(ids, mongoCollection, getEntityType());
    }

    @Override
    public List<T> findAll(long offset, int length) {
        return find(null, null, offset, length, getEntityType());
    }

    protected <R> List<R> find(Bson queryFilter, Bson sortCriteria, long offset, int length, Class<R> resultType) {
        return MongoDaoHelper.find(queryFilter, sortCriteria, offset, length, mongoCollection, resultType);
    }

    protected void insertNewEntity(T entity) {
        mongoCollection.insertOne(entity);
    }

    protected void insertNewEntities(List<T> entities) {
        if (CollectionUtils.isNotEmpty(entities)) {
            mongoCollection.insertMany(entities);
        }
    }

    @Override
    public void delete(T entity) {
        MongoDaoHelper.delete(mongoCollection, entity.getId());
    }

    @Override
    public DaoUpdateResult update(Long entityId, Map<String, EntityFieldValueHandler<?>> fieldsToUpdate) {
        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(false);
        return MongoDaoHelper.updateMany(mongoCollection, MongoDaoHelper.createFilterById(entityId), fieldsToUpdate, updateOptions);
    }

}
