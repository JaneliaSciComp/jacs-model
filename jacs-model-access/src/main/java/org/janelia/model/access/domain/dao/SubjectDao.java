package org.janelia.model.access.domain.dao;

import org.janelia.model.security.GroupRole;
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

    /**
     * @return all user subjects.
     */
    List<User> findAllUsers();

    /**
     * @return all group subjects
     */
    List<Group> findAllGroups();

    /**
     * Return the subject identified by the given subject key. Subject keys start with a user: or group: prefix.
     * @param key
     * @return
     */
    Subject findByKey(String key);

    /**
     * Return the subject identified by the given unique name.
     * @param name
     * @return
     */
    Subject findByName(String name);

    /**
     * Return the subject identified by the given subject key or name.
     * @param nameOrKey
     * @return
     */
    Subject findByNameOrKey(String nameOrKey);

    /**
     * Return the group identified by the given name or key.
     * @param groupNameOrKey must be a group name or key
     * @return the group identified by the name or key or null if none found
     */
    Group findGroupByNameOrKey(String groupNameOrKey);

    /**
     * Return the user identified by the given name or key.
     * @param userNameOrKey must be a user name or key
     * @return the user identified by the name or key or null if none found
     */
    User findUserByNameOrKey(String userNameOrKey);

    /**
     * Return all of the subjects which the given subject has access to read.
     * @param subjectKey
     * @return
     */
    Set<String> getReaderSetByKey(String subjectKey);

    /**
     * Return all of the subjects which the given subject has access to write.
     * @param subjectKey
     * @return
     */
    Set<String> getWriterSetByKey(String subjectKey);

    /**
     * Return all of the members of the group identified by the given name or key.
     * @param nameOrKey
     * @return
     */
    List<Subject> getGroupMembers(String nameOrKey);

    /**
     * Return a hash of groups which the number of members they have.
     * @return
     */
    Map<Subject, Number> getGroupMembersCount();

    /**
     * Create a new user with the given attributes.
     * @param name
     * @param fullName
     * @param email
     * @return
     */
    User createUser(String name, String fullName, String email);

    /**
     * Update a single property on a user.
     * @param user
     * @param property
     * @param value
     * @return
     */
    boolean updateUserProperty(User user, String property, String value);

    /**
     * Sets the user's password if users are being authenticated by JACS. If they are being authenticated externally,
     * then the password will be set in the database, but ignored for purposes of authentication.
     * @param user
     * @param passwordHash
     * @return
     */
    User setUserPassword(User user, String passwordHash);

    /**
     * Add user to group with specified role.
     *
     * @param userNameOrKey
     * @param groupNameOrKey
     * @param role
     */
    void addUserToGroup(String userNameOrKey, String groupNameOrKey, GroupRole role);

    /**
     * Remove user from a group.
     * @param userNameOrKey
     * @param groupNameOrKey
     */
    void removeUserFromGroup(String userNameOrKey, String groupNameOrKey);

    /**
     * Remove subject
     * @param key
     */
    void removeSubjectByKey(String key);

    /**
     * Update a user's group roles.
     * @param user
     * @param userGroupRoles
     * @return
     */
    boolean updateUserGroupRoles(User user, Set<UserGroupRole> userGroupRoles);

    /**
     * Create a new group with the given attributes.
     * @param name lowercase, single-word name for the group
     * @param fullName human-readable label for the group
     * @param ldapName corresponding group in LDAP, if any
     * @return
     */
    Group createGroup(String name, String fullName, String ldapName);

}
