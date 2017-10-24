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
    Owner("Owner", true, true),
    Admin("Admin", true, true),
    Writer("Writer", true, true),
    Reader("Reader", true, false);
    
    private String label;
    private boolean canRead;
    private boolean canWrite;
    
    private GroupRole(String label, boolean canRead, boolean canWrite) {
        this.label = label;
        this.canRead = canRead;
        this.canWrite = canWrite;
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
}
