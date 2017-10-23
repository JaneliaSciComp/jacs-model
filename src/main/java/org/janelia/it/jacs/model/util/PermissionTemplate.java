package org.janelia.it.jacs.model.util;

import java.io.Serializable;

/**
 * A permission not specific to any given entity.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class PermissionTemplate implements Serializable {

    private String subjectKey;
    private String permissions;

    public String getSubjectKey() {
        return subjectKey;
    }

    public void setSubjectKey(String subjectKey) {
        this.subjectKey = subjectKey;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }
}
