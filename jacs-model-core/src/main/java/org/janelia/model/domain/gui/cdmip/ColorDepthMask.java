package org.janelia.model.domain.gui.cdmip;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.interfaces.HasFilepath;
import org.janelia.model.domain.interfaces.HasFiles;
import org.janelia.model.domain.sample.AlignedImage2d;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.support.SearchType;

/**
 * A color depth mask is an image file which is used to search against the color depth image database.
 * It is also a node which can be used to organize color depth search matches
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="cdmipMask",label="Color Depth Mask")
@SearchType(key="cdmipMask",label="Color Depth Mask")
public class ColorDepthMask extends AlignedImage2d implements HasFilepath, HasFiles {

    @SearchTraversal({ColorDepthMask.class})
    private Reference sourceSampleRef;

    @SearchAttribute(key="threshold_i",label="Threshold for Mask")
    private Integer maskThreshold;

    public Reference getSample() {
        return sourceSampleRef;
    }

    public void setSample(Reference sample) {
        this.sourceSampleRef = sample;
    }

    public Integer getMaskThreshold() {
        return maskThreshold;
    }

    public void setMaskThreshold(Integer maskThreshold) {
        this.maskThreshold = maskThreshold;
    }

    @Override
    @JsonIgnore
    public Map<FileType, String> getFiles() {
        return ImmutableMap.of(FileType.Unclassified2d, getFilepath());
    }
}
