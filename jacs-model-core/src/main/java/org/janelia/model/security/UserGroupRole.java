package org.janelia.model.security;

/**
 * A user's role in a group.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class UserGroupRole {

    private String groupKey;
    private GroupRole role;
    
    public UserGroupRole() {
    }
    
    public UserGroupRole(String groupKey, GroupRole role) {
        this.groupKey = groupKey;
        this.role = role;
    }

    public String getGroupKey() {
        return groupKey;
    }
    
    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }
    
    public GroupRole getRole() {
        return role;
    }
    
    public void setRole(GroupRole role) {
        this.role = role;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserGroupRole[");
        if (groupKey != null) {
            builder.append("groupKey=");
            builder.append(groupKey);
            builder.append(", ");
        }
        if (role != null) {
            builder.append("role=");
            builder.append(role);
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groupKey == null) ? 0 : groupKey.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserGroupRole other = (UserGroupRole) obj;
        if (groupKey == null) {
            if (other.groupKey != null)
                return false;
        }
        else if (!groupKey.equals(other.groupKey))
            return false;
        if (role != other.role)
            return false;
        return true;
    }
}
