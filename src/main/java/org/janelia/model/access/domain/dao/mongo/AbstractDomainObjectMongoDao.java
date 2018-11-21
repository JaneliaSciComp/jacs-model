package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.janelia.model.access.domain.dao.DomainObjectDao;
import org.janelia.model.domain.DomainObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract Domain DAO that can handle entity access.
 *
 * @param <T> type of the element
 */
public abstract class AbstractDomainObjectMongoDao<T extends DomainObject>
        extends AbstractEntityMongoDao<T>
        implements DomainObjectDao<T> {

    final DomainPermissionsMongoHelper permissionsHelper;
    private final DomainUpdateMongoHelper updateHelper;

    AbstractDomainObjectMongoDao(MongoDatabase mongoDatabase,
                                 DomainPermissionsMongoHelper permissionsHelper,
                                 DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase);
        this.permissionsHelper = permissionsHelper;
        this.updateHelper = updateHelper;
    }

    @Override
    public List<T> findOwnedEntitiesBySubjectKey(String subjectKey, long offset, int length) {
        if (StringUtils.isNotBlank(subjectKey)) {
            return find(Filters.eq("ownerKey", subjectKey), null, offset, length, getEntityType());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public T findEntityByIdAccessibleBySubjectKey(Long id, String subjectKey) {
        if (id == null) {
            return null;
        } else {
            List<T> entities = find(
                    MongoDaoHelper.createFilterCriteria(
                            MongoDaoHelper.createFilterById(id),
                            permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey)),
                    null,
                    0,
                    -1,
                    getEntityType());
            if (CollectionUtils.isNotEmpty(entities)) {
                return entities.get(0);
            } else {
                return null;
            }
        }
    }

    @Override
    public List<T> findEntitiesByIdsAccessibleBySubjectKey(List<Long> ids, String subjectKey) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        } else {
            return find(
                    MongoDaoHelper.createFilterCriteria(
                            MongoDaoHelper.createFilterByIds(ids),
                            permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey)),
                    null,
                    0,
                    -1,
                    getEntityType());
        }
    }

    @Override
    public T saveBySubjectKey(T entity, String subjectKey) {
        Date now = new Date();
        if (entity.getId() == null) {
            entity.setId(createNewId());
            entity.setOwnerKey(subjectKey);
            entity.getReaders().add(subjectKey);
            entity.getWriters().add(subjectKey);
            entity.setCreationDate(now);
            entity.setUpdatedDate(now);
            mongoCollection.insertOne(entity);
        } else {
            entity.setUpdatedDate(now);
            mongoCollection.updateOne(
                    Filters.and(MongoDaoHelper.createFilterById(entity.getId()),
                            permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey)),
                    updateHelper.getEntityUpdates(entity)
            );
        }
        return entity;
    }

    @Override
    public void save(T entity) {
        Date now = new Date();
        if (entity.getId() == null) {
            entity.setId(createNewId());
            entity.setCreationDate(now);
            entity.setUpdatedDate(now);
            mongoCollection.insertOne(entity);
        }
    }

    @Override
    public void saveAll(Collection<T> entities) {
        Iterator<Long> idIterator = createNewIds(entities.size()).iterator();
        List<T> toInsert = new ArrayList<>();
        entities.forEach(e -> {
            if (e.getId() == null) {
                e.setId(idIterator.next());
                toInsert.add(e);
            }
        });
        if (!toInsert.isEmpty()) {
            mongoCollection.insertMany(toInsert);
        }
    }

    @Override
    public void delete(T entity) {
        MongoDaoHelper.delete(mongoCollection, entity.getId());
    }

    @Override
    public long deleteByIdAndSubjectKey(Long id, String subjectKey) {
        if (id == null) {
            return 0;
        } else {
            return MongoDaoHelper.deleteMatchingRecords(mongoCollection,
                    Filters.and(MongoDaoHelper.createFilterById(id), permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey)));
        }
    }
}
