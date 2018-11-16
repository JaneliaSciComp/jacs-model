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
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.dao.DomainObjectDao;
import org.janelia.model.access.domain.dao.SubjectDao;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.security.Subject;
import org.janelia.model.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

/**
 * Abstract Domain DAO that can handle entity access.
 *
 * @param <T> type of the element
 */
public abstract class AbstractPermissionAwareDomainMongoDao<T extends DomainObject>
        extends AbstractMongoDao<T>
        implements DomainObjectDao<T> {

    private final SubjectDao subjectDao;
    private final ObjectMapper objectMapper;

    AbstractPermissionAwareDomainMongoDao(MongoDatabase mongoDatabase, ObjectMapper objectMapper) {
        super(mongoDatabase);
        this.subjectDao = new SubjectMongoDao(mongoDatabase);
        this.objectMapper = objectMapper;
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
        if (id == null) {
            return null;
        } else {
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
    }

    @Override
    public List<T> findByIdsAndSubjectKey(List<Long> ids, String subjectKey) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        } else {
            return find(MongoDaoHelper.createFilterCriteria(
                    ImmutableList.of(MongoDaoHelper.createFilterByIds(ids), createSubjectReadPermissionFilter(subjectKey))),
                    null, 0, -1,
                    getEntityType());
        }
    }

    Bson createSubjectReadPermissionFilter(String subjectKey) {
        Set<String> readers = subjectDao.getReaderSetByKey(subjectKey);
        if (CollectionUtils.isEmpty(readers)) {
            // only include entities that have no reader restrictions
            return Filters.or(
                    Filters.eq("ownerKey", subjectKey),
                    Filters.exists("readers", false)
            );
        } else if (readers.contains(Subject.ADMIN_KEY)) {
            return Filters.and(); // user is in the admin group so simply ignore the filtering in this case
        } else {
            return Filters.or(
                    Filters.eq("ownerKey", subjectKey),
                    Filters.in("readers", readers)
            );
        }
    }

    Bson createSubjectWritePermissionFilter(String subjectKey) {
        Set<String> readers = subjectDao.getWriterSetByKey(subjectKey);
        if (CollectionUtils.isEmpty(readers)) {
            // only include entities that have no reader restrictions
            return Filters.or(
                    Filters.eq("ownerKey", subjectKey),
                    Filters.exists("readers", false)
            );
        } else if (readers.contains(Subject.ADMIN_KEY)) {
            return Filters.and(); // user is in the admin group so simply ignore the filtering in this case
        } else {
            return Filters.or(
                    Filters.eq("ownerKey", subjectKey),
                    Filters.in("readers", readers)
            );
        }
    }

    @Override
    public T saveWithSubjectKey(T entity, String subjectKey) {
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
                            createSubjectWritePermissionFilter(subjectKey)),
                    Updates.combine(getEntityUpdates(entity))
            );
        }
        return entity;
    }

    private List<Bson> getEntityUpdates(T entity) {
        try {
            String jsonEntity = objectMapper.writeValueAsString(entity);
            Document bsonEntity = Document.parse(jsonEntity);
            return bsonEntity.entrySet().stream().map(e -> {
                Object value = e.getValue();
                if (value == null) {
                    return Updates.unset(e.getKey());
                } else {
                    return Updates.set(e.getKey(), e.getValue());
                }
            }).collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
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

    @Override
    public long deleteByIdAndSubjectKey(Long id, String subjectKey) {
        if (id == null) {
            return 0;
        } else {
            return MongoDaoHelper.deleteMatchingRecords(mongoCollection,
                    Filters.and(MongoDaoHelper.createFilterById(id), createSubjectWritePermissionFilter(subjectKey)));
        }
    }
}
