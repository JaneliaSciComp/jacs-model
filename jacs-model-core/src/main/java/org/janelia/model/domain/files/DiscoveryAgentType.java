package org.janelia.model.domain.files;

import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.tiledMicroscope.TmSample;

/**
 * Discovery agents available on the backend. Each discovery agent can discover a particular type of domain object.
 *
 * The enumerated names here correspond to @Named services in the jacs-async services.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public enum DiscoveryAgentType {

    n5DiscoveryAgent("N5 Containers", N5Container.class),
    zarrDiscoveryAgent("Zarr Containers", ZarrContainer.class),
    hortaDiscoveryAgent("Horta Samples", TmSample.class);

    private final String label;
    private final Class<? extends DomainObject> domainObjectClass;

    DiscoveryAgentType(String label, Class<? extends DomainObject> domainObjectClass) {
        this.label = label;
        this.domainObjectClass = domainObjectClass;
    }
    
    public String getLabel() {
        return label;
    }

    public Class<? extends DomainObject> getDomainObjectClass() {
        return domainObjectClass;
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
