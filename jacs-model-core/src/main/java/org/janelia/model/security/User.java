package org.janelia.model.security;

import java.util.HashSet;
import java.util.Set;

/**
 * A user in the Workstation system who can be part of one or more groups. 
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class User extends Subject {

    private String password;
    private String email;
    private Set<UserGroupRole> userGroupRoles = new HashSet<>();

    /**
     * Hashed password. This is not a required field. If this is undefined, it's expected that the user will be
     * authenticated against an external service such as LDAP or AD.
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<UserGroupRole> getUserGroupRoles() {
        return userGroupRoles;
    }

    public void setUserGroupRoles(Set<UserGroupRole> userGroupRoles) {
        if (userGroupRoles==null) throw new IllegalArgumentException("Property cannot be null");
        this.userGroupRoles = userGroupRoles;
    }
    
    public void setUserGroupRole(String groupKey, GroupRole role) {
        UserGroupRole ugr = getRole(groupKey);
        if (ugr==null) {
            userGroupRoles.add(new UserGroupRole(groupKey, role));
        }
        else {
            ugr.setRole(role);
        }
    }
    
    public GroupRole getUserGroupRole(String groupKey) {
        UserGroupRole ugr = getRole(groupKey);
        return ugr==null ? null : ugr.getRole();
    }
    
    public Set<String> getReadGroups() {
        Set<String> groups = new HashSet<>();
        for(UserGroupRole groupRole : userGroupRoles) {
            if (groupRole.getRole().isRead()) {
                groups.add(groupRole.getGroupKey());
            }
        }
        return groups;
    }

    public Set<String> getWriteGroups() {
        Set<String> groups = new HashSet<>();
        for(UserGroupRole groupRole : userGroupRoles) {
            if (groupRole.getRole().isWrite()) {
                groups.add(groupRole.getGroupKey());
            }
        }
        return groups;
    }
    
    public boolean hasGroupRead(String groupKey) {
        UserGroupRole role = getRole(groupKey);
        return role!=null && role.getRole().isRead();
    }

    public boolean hasGroupWrite(String groupKey) {
        UserGroupRole role = getRole(groupKey);
        return role!=null && role.getRole().isWrite();
    }
    
    public UserGroupRole getRole(String groupKey) {
        for(UserGroupRole groupRole : userGroupRoles) {
            if (groupRole.getGroupKey().equals(groupKey)) {
                return groupRole;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("User[");
        if (getId() != null) {
            builder.append("id=");
            builder.append(getId());
            builder.append(", ");
        }
        if (getKey() != null) {
            builder.append("key=");
            builder.append(getKey());
            builder.append(", ");
        }
        if (getName() != null) {
            builder.append("name=");
            builder.append(getName());
            builder.append(", ");
        }
        if (getFullName() != null) {
            builder.append("fullName=");
            builder.append(getFullName());
            builder.append(", ");
        }
        if (email != null) {
            builder.append("email=");
            builder.append(email);
        }
        builder.append("]");
        return builder.toString();
    }
}
