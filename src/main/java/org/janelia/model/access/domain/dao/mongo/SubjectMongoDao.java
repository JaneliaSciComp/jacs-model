package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.janelia.model.access.domain.dao.SubjectDao;
import org.janelia.model.security.Subject;
import org.janelia.model.security.util.SubjectUtils;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Subject Mongo DAO.
 */
public class SubjectMongoDao extends AbstractEntityMongoDao<Subject> implements SubjectDao {

    @Inject
    public SubjectMongoDao(MongoDatabase mongoDatabase) {
        super(mongoDatabase);
    }

    @Override
    public void save(Subject entity) {
        if (entity.getId() == null) {
            entity.setId(createNewId());
            insertNewEntity(entity);
        }
    }

    @Override
    public void saveAll(Collection<Subject> entities) {
        Iterator<Long> idIterator = createNewIds(entities.size()).iterator();
        List<Subject> toInsert = new ArrayList<>();
        entities.forEach(e -> {
            if (e.getId() == null) {
                e.setId(idIterator.next());
                toInsert.add(e);
            }
        });
        insertNewEntities(toInsert);
    }

    @Override
    public Subject findByKey(String key) {
        if (StringUtils.isNotBlank(key)) {
            List<Subject> subjects = find(Filters.eq("key", key), null, 0, -1, getEntityType());
            if (CollectionUtils.isNotEmpty(subjects)) {
                return subjects.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Subject findByName(String name) {
        if (StringUtils.isNotBlank(name)) {
            List<Subject> subjects = find(Filters.eq("name", name), null, 0, -1, getEntityType());
            if (CollectionUtils.isNotEmpty(subjects)) {
                return subjects.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Subject findByNameOrKey(String nameOrKey) {
        if (StringUtils.isNotBlank(nameOrKey)) {
            List<Subject> subjects = find(
                    Filters.or(Filters.eq("key", nameOrKey), Filters.eq("name", nameOrKey)),
                    null,
                    0,
                    -1,
                    getEntityType());
            if (CollectionUtils.isNotEmpty(subjects)) {
                return subjects.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Set<String> getReaderSetByKey(String subjectKey) {
        Subject subject = findByKey(subjectKey);
        if (subject == null) {
            return Collections.emptySet();
        } else {
            return SubjectUtils.getReaderSet(subject);
        }
    }

    @Override
    public Set<String> getWriterSetByKey(String subjectKey) {
        Subject subject = findByKey(subjectKey);
        if (subject == null) {
            return Collections.emptySet();
        } else {
            return SubjectUtils.getWriterSet(subject);
        }
    }

    @Override
    public List<Subject> getGroupMembers(String nameOrKey) {
        String groupName = SubjectUtils.getSubjectName(nameOrKey);
        String groupKey = "group:" + groupName;
        return MongoDaoHelper.find(
                Filters.eq("userGroupRoles.groupKey", groupKey),
                null,
                0,
                -1,
                mongoCollection,
                Subject.class
        );
    }

    @Override
    public Map<Subject, Number> getGroupMembersCount() {
        List<Subject> distinctGroups = MongoDaoHelper.getDistinctValues("userGroupRoles.groupKey", null, mongoCollection, Subject.class);
        return distinctGroups.stream()
                .collect(Collectors.toMap(
                        s -> s,
                        s -> MongoDaoHelper.count(
                                    Filters.eq("userGroupRoles.groupKey", s),
                                    mongoCollection)));
    }
}
