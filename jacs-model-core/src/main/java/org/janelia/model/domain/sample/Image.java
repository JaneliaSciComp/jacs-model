package org.janelia.model.domain.sample;

import java.util.HashMap;
import java.util.Map;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.interfaces.HasRelativeFiles;
import org.janelia.model.domain.interfaces.IsAligned;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SAGEAttribute;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchType;

/**
 * An image file on disk, and related metadata, such as where to find summary files such as MIPs or movies.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="image",label="Image")
@SearchType(key="image",label="Image")
public class Image extends AbstractDomainObject implements HasRelativeFiles, IsAligned {

    @SearchAttribute(key="filepath_txt",label="File Path")
    private String filepath;
    
    @SearchAttribute(key="image_size_s",label="Image Size")
    private String imageSize;

    @SearchAttribute(key="optical_res_s",label="Optical Resolution")
    private String opticalResolution;

    @SearchAttribute(key="objective_txt",label="Objective", facet="objective_s")
    private String objective;

    @SAGEAttribute(cvName="light_imagery", termName="channels")
    @SearchAttribute(key="num_channels_i",label="Num Channels", facet="num_channels_i")
    private Integer numChannels;

    @SearchAttribute(key="alignment_s",label="Alignment Space", facet="alignment_s")
    private String alignmentSpace;

    private Boolean userDataFlag;

    private Map<FileType, String> files = new HashMap<>();

    @Override
    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    public String getOpticalResolution() {
        return opticalResolution;
    }

    public void setOpticalResolution(String opticalResolution) {
        this.opticalResolution = opticalResolution;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public Integer getNumChannels() {
        return numChannels;
    }

    public void setNumChannels(Integer numChannels) {
        this.numChannels = numChannels;
    }

    public String getAlignmentSpace() {
        return alignmentSpace;
    }

    public void setAlignmentSpace(String alignmentSpace) {
        this.alignmentSpace = alignmentSpace;
    }

    public Boolean getUserDataFlag() {
        return userDataFlag;
    }

    public void setUserDataFlag(Boolean userDataFlag) {
        this.userDataFlag = userDataFlag;
    }

    @Override
    /**
     * Use DomainUtils.getFilepath instead of this method, to get the absolute path. This method is only here to support serialization.
     */
    public Map<FileType, String> getFiles() {
        return files;
    }

    public void setFiles(Map<FileType, String> files) {
        if (files==null) throw new IllegalArgumentException("Property cannot be null");
        this.files = files;
    }
}