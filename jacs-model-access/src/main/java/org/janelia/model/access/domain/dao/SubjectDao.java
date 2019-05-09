package org.janelia.model.access.domain.dao;

import org.janelia.model.security.Subject;
import org.janelia.model.security.User;
import org.janelia.model.security.Group;
import org.janelia.model.security.UserGroupRole;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for accessing subject info.
 */
public interface SubjectDao extends ReadDao<Subject, Long>, WriteDao<Subject, Long> {

    Subject findByKey(String key);

    Subject findByName(String name);

    Subject findByNameOrKey(String nameOrKey);

    Set<String> getReaderSetByKey(String subjectKey);

    Set<String> getWriterSetByKey(String subjectKey);

    List<Subject> getGroupMembers(String nameOrKey);

    Map<Subject, Number> getGroupMembersCount();

    /**
     * Sets the user's password if users are being authenticated by JACS. If they are being authenticated externally,
     * than this method has no effect.
     * @param user
     * @param passwordHash
     * @return
     */
    User setUserPassword(User user, String passwordHash);

    boolean updateUserGroupRoles(User user, Set<UserGroupRole> userGroupRoles);

    Group createGroup(String groupKey, String fullName, String ldapName);

    boolean updateUserProperty(User user, String property, String value);

}
