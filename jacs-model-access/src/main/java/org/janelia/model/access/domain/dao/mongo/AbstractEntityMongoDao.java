package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.IdGenerator;
import org.janelia.model.access.domain.dao.DaoUpdateResult;
import org.janelia.model.access.domain.dao.EntityFieldValueHandler;
import org.janelia.model.access.domain.dao.ReadDao;
import org.janelia.model.access.domain.dao.WriteDao;
import org.janelia.model.domain.interfaces.HasIdentifier;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Abstract Mongo DAO.
 *
 * @param <T> type of the element
 */
public abstract class AbstractEntityMongoDao<T extends HasIdentifier>
        extends AbstractMongoDao
        implements ReadDao<T, Long>, WriteDao<T, Long> {

    final MongoCollection<T> mongoCollection;

    AbstractEntityMongoDao(MongoDatabase mongoDatabase, IdGenerator<Long> idGenerator) {
        super(mongoDatabase, idGenerator);
        Class<T> entityClass = getEntityType();
        mongoCollection = getEntityCollection(entityClass);
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

    <R> List<R> find(Bson queryFilter, Bson sortCriteria, long offset, int length, Class<R> resultType) {
        return MongoDaoHelper.find(queryFilter, sortCriteria, offset, length, mongoCollection, resultType);
    }

    void insertNewEntity(T entity) {
        mongoCollection.insertOne(entity);
    }

    void insertNewEntities(List<T> entities) {
        if (CollectionUtils.isNotEmpty(entities)) {
            mongoCollection.insertMany(entities);
        }
    }

    @Override
    public void delete(T entity) {
        MongoDaoHelper.delete(mongoCollection, entity.getId());
    }

    @Override
    public void replace(T entity) {
        MongoDaoHelper.replace(mongoCollection, entity.getId(), entity);
    }

    @Override
    public DaoUpdateResult update(Long entityId, Map<String, EntityFieldValueHandler<?>> fieldsToUpdate) {
        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(false);
        return MongoDaoHelper.updateMany(mongoCollection, MongoDaoHelper.createFilterById(entityId), fieldsToUpdate, updateOptions);
    }

}
