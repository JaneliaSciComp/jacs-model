package org.janelia.model.mongo;

import com.google.common.collect.ImmutableMap;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.janelia.model.access.domain.dao.DaoUpdateResult;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.access.domain.dao.SubjectDao;
import org.janelia.model.security.*;
import org.janelia.model.security.util.SubjectUtils;

import javax.inject.Inject;
import java.beans.PropertyDescriptor;
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
        else {
            throw new IllegalArgumentException("Cannot save object which already has an id");
        }
    }

    @Override
    public User setUserPassword(User user, String passwordHash) {

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(false);
        DaoUpdateResult updateResult = MongoDaoHelper.updateMany(
                mongoCollection,
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createFilterById(user.getId())
                ),
                ImmutableMap.of(
                    "password", new SetFieldValueHandler<>(passwordHash)
                ),
                updateOptions);
        if (updateResult.getEntitiesFound() == 0) {
            // no entity was found for update - this usually happens if the user does not have write permissions
            return null;
        } else {
            user.setPassword(passwordHash);
            return user;
        }
    }

    @Override
    public boolean updateUserProperty(User user, String property, String value) {

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(false);
        DaoUpdateResult updateResult = MongoDaoHelper.updateMany(
                mongoCollection,
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createFilterById(user.getId())
                ),
                ImmutableMap.of(
                        property, new SetFieldValueHandler<>(value)
                ),
                updateOptions);
        if (updateResult.getEntitiesFound() == 0) {
            // no entity was found for update - this usually happens if the user does not have write permissions
            return false;
        } else {
            try {
                new PropertyDescriptor(property, User.class).getWriteMethod().invoke(user, value);
            } catch (Exception e) {
                throw new RuntimeException ("Problem try to change property" + property + " on User");
            }
            return true;
        }
    }

    @Override
    public boolean updateUserGroupRoles(User user, Set<UserGroupRole> groupRoles) {

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(false);
        DaoUpdateResult updateResult = MongoDaoHelper.updateMany(
                mongoCollection,
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createFilterById(user.getId())
                ),
                ImmutableMap.of(
                        "userGroupRoles", new SetFieldValueHandler<>(groupRoles)
                ),
                updateOptions);
        if (updateResult.getEntitiesFound() == 0) {
            // no entity was found for update - this usually happens if the user does not have write permissions
            return false;
        }
        return true;
    }

    @Override
    public Group createGroup(String groupKey, String fullName, String ldapName) {
        Group newGroup = new Group();
        newGroup.setLdapGroupName(ldapName);
        newGroup.setFullName(fullName);
        newGroup.setKey(groupKey);
        save(newGroup);
        newGroup = (Group) findByKey(groupKey);
        return newGroup;
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
            else {
                throw new IllegalArgumentException("Cannot save object which already has an id");
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
        List<String> distinctGroupNames = MongoDaoHelper.getDistinctValues("userGroupRoles.groupKey", null, mongoCollection, String.class);
        return distinctGroupNames.stream()
                .map(gn -> MongoDaoHelper.findFirst(Filters.eq("key", gn), null, mongoCollection, Subject.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        s -> s,
                        s -> MongoDaoHelper.count(
                                    Filters.eq("userGroupRoles.groupKey", s.getKey()),
                                    mongoCollection)));
    }
}