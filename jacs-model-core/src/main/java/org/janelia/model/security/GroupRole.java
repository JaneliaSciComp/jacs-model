package org.janelia.model.security;

import java.io.Serializable;

/**
 * Enumeration of possible group roles. The roles can confer different privileges.
 * For now, the privileges are defined as read/write, or by hard-coding against the 
 * actual role value. 
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public enum GroupRole implements Serializable {
    Owner("Owner", true, true, true),
    Admin("Admin", true, true, true),
    Writer("Writer", true, true, false),
    Reader("Reader", true, false, false);
    
    private String label;
    private boolean canRead;
    private boolean canWrite;
    private boolean canAdmin;
    
    GroupRole(String label, boolean canRead, boolean canWrite, boolean canAdmin) {
        this.label = label;
        this.canRead = canRead;
        this.canWrite = canWrite;
        this.canAdmin = canAdmin;
    }

    public String getLabel() {
        return label;
    }

    public boolean isRead() {
        return canRead;
    }

    public boolean isWrite() {
        return canWrite;
    }

    public boolean isAdmin() {
        return canAdmin;
    }

    public boolean isOwner() {
        return this==Owner;
    }
}
