package org.janelia.model.security;

/**
 * A group in the Workstation system for aggregating users. 
 * 
 * Users may have different roles in the group, and these are defined 
 * in the UserGroupRoles present in the Users. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class Group extends Subject {

    private String ldapGroupName;
    
    public String getLdapGroupName() {
        return ldapGroupName;
    }

    public void setLdapGroupName(String ldapGroupName) {
        this.ldapGroupName = ldapGroupName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Group[");
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
        }
        builder.append("]");
        return builder.toString();
    }
}
