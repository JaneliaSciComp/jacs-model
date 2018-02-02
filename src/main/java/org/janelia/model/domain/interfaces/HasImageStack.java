package org.janelia.model.domain.interfaces;

import org.janelia.model.access.domain.DomainUtils;
import org.janelia.model.domain.enums.FileType;

/**
 * Methods for exposing an underlying image stack.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface HasImageStack extends HasFiles {

    default String getLosslessStack() {
        return DomainUtils.getFilepath(this, FileType.LosslessStack);
    }

    default String getVisuallyLosslessStack() {
        return DomainUtils.getFilepath(this, FileType.VisuallyLosslessStack);
    }

    public String getAnatomicalArea();

    public String getImageSize();

    public String getOpticalResolution();

    public String getChannelColors();

    public String getChannelSpec();

}