package org.janelia.model.domain.gui.colordepth;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.support.MongoMapped;

import java.util.Map;

/**
 * The result of running a ColorDepthSearch on the cluster. Each mask has a list of ColorDepthResults associated
 * with it.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="colorDepthResult",label="Color Depth Result")
public class ColorDepthResult extends AbstractDomainObject {

    private Map<Reference,ColorDepthMatch> matches;

    public Map<Reference, ColorDepthMatch> getMatches() {
        return matches;
    }

    public void setMatches(Map<Reference, ColorDepthMatch> matches) {
        this.matches = matches;
    }
}
