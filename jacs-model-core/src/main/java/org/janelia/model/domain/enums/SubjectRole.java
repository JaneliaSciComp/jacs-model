package org.janelia.model.domain.enums;

/**
 * Enumeration of standard roles in the system.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public enum SubjectRole {

    Admin("group:admin"),
    WorkstationUsers("group:workstation_users");

    private String role;

    SubjectRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return role;
    }
}
