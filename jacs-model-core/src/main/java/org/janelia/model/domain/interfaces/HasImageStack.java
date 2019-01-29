package org.janelia.model.domain.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.access.domain.DomainUtils;
import org.janelia.model.domain.enums.FileType;

/**
 * Methods for exposing an underlying image stack.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface HasImageStack extends HasFiles {

    @JsonIgnore
    default String getLosslessStack() {
        return DomainUtils.getFilepath(this, FileType.LosslessStack);
    }

    @JsonIgnore
    default String getVisuallyLosslessStack() {
        return DomainUtils.getFilepath(this, FileType.VisuallyLosslessStack);
    }

    String getAnatomicalArea();

    String getImageSize();

    String getOpticalResolution();

    String getChannelColors();

    String getChannelSpec();

}