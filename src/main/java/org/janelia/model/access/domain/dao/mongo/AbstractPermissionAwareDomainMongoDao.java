package org.janelia.model.access.domain.dao.mongo;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.DomainUtils;
import org.janelia.model.access.domain.dao.DomainObjectDao;
import org.janelia.model.access.domain.dao.EntityUtils;
import org.janelia.model.access.domain.dao.SubjectDao;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.security.Subject;
import org.janelia.model.util.TimebasedIdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Abstract Domain DAO that can handle entity access.
 *
 * @param <T> type of the element
 */
public abstract class AbstractPermissionAwareDomainMongoDao<T extends DomainObject>
        extends AbstractMongoDao<T>
        implements DomainObjectDao<T> {

    private final SubjectDao subjectDao;

    AbstractPermissionAwareDomainMongoDao(MongoDatabase mongoDatabase) {
        super(mongoDatabase);
        subjectDao = new SubjectMongoDao(mongoDatabase);
    }

    @Override
    public List<T> findByOwnerKey(String ownerKey) {
        if (StringUtils.isNotBlank(ownerKey)) {
            return find(Filters.eq("ownerKey", ownerKey), null, 0, -1, getEntityType());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public T findByIdAndSubjectKey(Long id, String subjectKey) {
        List<T> entities = find(MongoDaoHelper.createFilterCriteria(
                ImmutableList.of(MongoDaoHelper.createFilterById(id), createSubjectReadPermissionFilter(subjectKey))),
                null, 0, -1,
                getEntityType());
        if (CollectionUtils.isNotEmpty(entities)) {
            return entities.get(0);
        } else {
            return null;
        }
    }

    private Bson createSubjectReadPermissionFilter(String subjectKey) {
        Set<String> readers = subjectDao.getReaderSetByKey(subjectKey);
        if (CollectionUtils.isEmpty(readers)) {
            // only include entities that have no reader restrictions
            return Filters.or(
                    Filters.eq("ownerKey", subjectKey),
                    Filters.exists("readers", false)
            );
        } else {
            return Filters.or(
                    Filters.eq("ownerKey", subjectKey),
                    Filters.in("readers", readers)
            );
        }
    }

    @Override
    public void save(T entity) {
        if (entity.getId() == null) {
            entity.setId(createNewId());
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
}
