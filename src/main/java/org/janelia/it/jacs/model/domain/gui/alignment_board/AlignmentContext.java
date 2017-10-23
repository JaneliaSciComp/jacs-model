package org.janelia.it.jacs.model.domain.gui.alignment_board;

import org.janelia.it.jacs.model.domain.AbstractDomainObject;
import org.janelia.it.jacs.model.domain.support.MongoMapped;

/**
 * Collection of alignment contexts.  No combination of these values
 * should be used in any other collection (they are denormalized) if
 * it does not first appear as one object in this collection.
 *
 * @author fosterl
 */
@MongoMapped(collectionName="alignmentContext",label="Alignment Context")
public class AlignmentContext extends AbstractDomainObject {
    private String imageSize;
    private String opticalResolution;
    private String alignmentSpace;

    /**
     * @return the imageSize
     */
    public String getImageSize() {
        return imageSize;
    }

    /**
     * @param imageSize the imageSize to set
     */
    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    /**
     * @return the opticalResolution
     */
    public String getOpticalResolution() {
        return opticalResolution;
    }

    /**
     * @param opticalResolution the opticalResolution to set
     */
    public void setOpticalResolution(String opticalResolution) {
        this.opticalResolution = opticalResolution;
    }

    /**
     * @return the alignmentSpace
     */
    public String getAlignmentSpace() {
        return alignmentSpace;
    }

    /**
     * @param alignmentSpace the alignmentSpace to set
     */
    public void setAlignmentSpace(String alignmentSpace) {
        this.alignmentSpace = alignmentSpace;
    }
    
}
