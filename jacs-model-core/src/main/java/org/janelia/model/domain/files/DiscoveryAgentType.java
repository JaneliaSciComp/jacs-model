package org.janelia.model.domain.files;

/**
 * Discovery agents available on the backend. The enumerated names here correspond to @Named services in the jacs-async
 * services.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public enum DiscoveryAgentType {

    n5DiscoveryAgent("N5 Containers"),
    zarrDiscoveryAgent("Zarr Containers"),
    hortaDiscoveryAgent("Horta Samples");

    private final String label;

    DiscoveryAgentType(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }

    /**
     * Given a label, return the corresponding enumerated type value. If the label does not exist, this return null.
     * @param label label of one of the values in this class
     * @return corresponding value or null if none
     */
    public static DiscoveryAgentType getByLabel(String label) {
        DiscoveryAgentType[] values = DiscoveryAgentType.values();
        for (DiscoveryAgentType value : values) {
            if (value.getLabel().equals(label)) {
                return value;
            }
        }
        return null;
    }
}
