package org.janelia.model.access.domain.dao.mongo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationStrength;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.janelia.model.access.domain.dao.AddToSetFieldValueHandler;
import org.janelia.model.access.domain.dao.DaoUpdateResult;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.access.domain.dao.SubjectDao;
import org.janelia.model.security.*;
import org.janelia.model.security.util.SubjectUtils;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Subject Mongo DAO.
 */
public class SubjectMongoDao extends AbstractEntityMongoDao<Subject> implements SubjectDao {

    private static final Logger log = LoggerFactory.getLogger(SubjectMongoDao.class);

    @Inject
    public SubjectMongoDao(MongoDatabase mongoDatabase, TimebasedIdentifierGenerator idGenerator) {
        super(mongoDatabase, idGenerator);
    }

    @Override
    public User createUser(String name, String fullName, String email) {
        log.debug("createUser(name={}, fullName={}, email={})", name, fullName, email);
        User newSubject = new User();
        String uname = name.toLowerCase();
        newSubject.setName(uname);
        newSubject.setKey("user:" + uname);
        newSubject.setFullName(fullName);
        newSubject.setEmail(email);
        save(newSubject);

        User user = (User) findSubjectByName(name);
        if (user == null) {
            throw new IllegalStateException("Problem creating user " + name);
        }

        log.debug("Created user " + user.getKey());
        return user;
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
    public void save(Subject entity) {
        if (entity.getId() == null) {
            entity.setId(createNewId());
            // force the key and name to lower case
            if (StringUtils.isNotBlank(entity.getKey())) {
                entity.setKey(entity.getKey().toLowerCase());
            }
            if (StringUtils.isNotBlank(entity.getName())) {
                entity.setName(entity.getName().toLowerCase());
            }
            insertNewEntity(entity);
        } else {
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
    public void addUserToGroup(String userNameOrKey, String groupNameOrKey, GroupRole role) {
        User u = findUserByNameOrKey(userNameOrKey);
        Preconditions.checkArgument(u != null, "No user found for " + userNameOrKey);
        Group g = findGroupByNameOrKey(groupNameOrKey);
        Preconditions.checkArgument(g != null, "No group found for " + groupNameOrKey);
        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(false);
        MongoDaoHelper.updateMany(
                mongoCollection,
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createFilterById(u.getId())
                ),
                ImmutableMap.of(
                        "userGroupRoles", new AddToSetFieldValueHandler<>(new UserGroupRole(g.getKey(), role))
                ),
                updateOptions);
    }

    @Override
    public void removeUserFromGroup(String userNameOrKey, String groupKey) {
        User u = findUserByNameOrKey(userNameOrKey);
        Preconditions.checkArgument(u != null, "No user found for " + userNameOrKey);
        Set<UserGroupRole> userGroupRoles = u.getUserGroupRoles().stream()
                .filter(ugr -> !StringUtils.equalsIgnoreCase(ugr.getGroupKey(), groupKey))
                .collect(Collectors.toSet());
        if (userGroupRoles.size() != u.getUserGroupRoles().size()) {
            updateUserGroupRoles(u, userGroupRoles);
        }
    }

    @Override
    public void removeSubjectByKey(String key) {
        if (StringUtils.isNotBlank(key)) {
            MongoDaoHelper.deleteMatchingRecords(
                    mongoCollection,
                    MongoDaoHelper.createAttributeFilter("key", key.toLowerCase()));
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
    public Group createGroup(String name, String fullName, String ldapName) {
        Group newGroup = new Group();
        String gname = name.toLowerCase();
        newGroup.setName(gname);
        newGroup.setKey("group:"+gname);
        newGroup.setLdapGroupName(ldapName);
        newGroup.setFullName(fullName);
        save(newGroup);

        Group group = (Group) findSubjectByName(name);
        if (group == null) {
            throw new IllegalStateException("Problem creating group " + name);
        }

        log.debug("Created group " + group.getKey());
        return group;
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
    public List<User> findAllUsers() {
        return find(Filters.regex("key", "^user:"), null, 0, -1, User.class);
    }

    @Override
    public List<Group> findAllGroups() {
        return find(Filters.regex("key", "^group:"), null, 0, -1, Group.class);
    }

    @Override
    public Subject findSubjectByKey(String key) {
        if (StringUtils.isNotBlank(key)) {
            String searchedKey = key.toLowerCase();
            List<Subject> subjects = find(
                    Filters.eq("key", searchedKey),
                    null,
                    0, -1, getEntityType());
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
    public Subject findSubjectByName(String name) {
        if (StringUtils.isNotBlank(name)) {
            String searchedName = name.toLowerCase();
            List<Subject> subjects = find(Filters.eq("name", searchedName),
                    null,
                    0, -1,
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
    public Subject findSubjectByNameOrKey(String nameOrKey) {
        if (StringUtils.isNotBlank(nameOrKey)) {
            String searchedNameOrKey = nameOrKey.toLowerCase();
            List<Subject> subjects = find(
                    Filters.or(
                            Filters.eq("key", searchedNameOrKey),
                            Filters.eq("name", searchedNameOrKey)),
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
    public Group findGroupByNameOrKey(String groupNameOrKey) {
        if (StringUtils.isNotBlank(groupNameOrKey)) {
            String searchedName = groupNameOrKey.toLowerCase();
            List<Group> groups = find(
                    Filters.and(
                            Filters.or(Filters.eq("key", searchedName), Filters.eq("name", searchedName)),
                            Filters.eq("class", Group.class.getName())
                    ),
                    null,
                    0,
                    -1,
                    Group.class);
            if (CollectionUtils.isNotEmpty(groups)) {
                return groups.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public User findUserByNameOrKey(String userNameOrKey) {
        if (StringUtils.isNotBlank(userNameOrKey)) {
            String searchedNameOrKey = userNameOrKey.toLowerCase();
            List<User> users = find(
                    Filters.and(
                            Filters.or(
                                    Filters.eq("key", searchedNameOrKey),
                                    Filters.eq("name", searchedNameOrKey)),
                            Filters.eq("class", User.class.getName())
                    ),
                    null,
                    0,
                    -1,
                    User.class);
            if (CollectionUtils.isNotEmpty(users)) {
                return users.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Set<String> getReaderSetByKey(String subjectKey) {
        Subject subject = findSubjectByKey(subjectKey);
        if (subject == null) {
            return Collections.emptySet();
        } else {
            return SubjectUtils.getReaderSet(subject);
        }
    }

    @Override
    public Set<String> getWriterSetByKey(String subjectKey) {
        Subject subject = findSubjectByKey(subjectKey);
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
