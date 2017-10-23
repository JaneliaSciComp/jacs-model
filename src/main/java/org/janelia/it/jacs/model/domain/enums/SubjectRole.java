package org.janelia.it.jacs.model.domain.enums;

/**
 * The type of image to show for given sample.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public enum SubjectRole {

    Admin("group:admin"),
    WorkstationUsers("group:workstation_users");

    private String role;

    private SubjectRole(String role) {
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
