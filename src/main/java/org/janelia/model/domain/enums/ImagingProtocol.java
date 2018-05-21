package org.janelia.model.domain.enums;

/**
 * The acquisition protocol used during imaging.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public enum ImagingProtocol {

    Screen("Screen"),
    Polarity("Polarity"),
    MCFO("MCFO (Multi-color Flp-out)"),
    FISH("FISH (Fluorescence In Situ Hybridization)");

    private String role;

    ImagingProtocol(String role) {
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
