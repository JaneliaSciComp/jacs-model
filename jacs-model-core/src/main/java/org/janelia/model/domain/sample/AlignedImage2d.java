package org.janelia.model.domain.sample;

import org.janelia.model.domain.interfaces.IsAligned;
import org.janelia.model.domain.support.SearchAttribute;

/**
 * An image in a given alignment space.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class AlignedImage2d extends Image2d implements IsAligned  {

    @SearchAttribute(key="alignment_s",label="Alignment Space", facet="alignment_s")
    private String alignmentSpace;

    public String getAlignmentSpace() {
        return alignmentSpace;
    }

    public void setAlignmentSpace(String alignmentSpace) {
        this.alignmentSpace = alignmentSpace;
    }
}
