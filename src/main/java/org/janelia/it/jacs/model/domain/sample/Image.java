package org.janelia.it.jacs.model.domain.sample;

import java.util.HashMap;
import java.util.Map;

import org.janelia.it.jacs.model.domain.AbstractDomainObject;
import org.janelia.it.jacs.model.domain.enums.FileType;
import org.janelia.it.jacs.model.domain.interfaces.HasRelativeFiles;
import org.janelia.it.jacs.model.domain.support.MongoMapped;
import org.janelia.it.jacs.model.domain.support.SAGEAttribute;
import org.janelia.it.jacs.model.domain.support.SearchAttribute;
import org.janelia.it.jacs.model.domain.support.SearchType;

/**
 * An image file on disk, and related metadata, such as where to find summary files such as MIPs or movies.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="image",label="Image")
@SearchType(key="image",label="Image")
public class Image extends AbstractDomainObject implements HasRelativeFiles {

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

    public Integer getNumChannels() {
        return numChannels;
    }

    public void setNumChannels(Integer numChannels) {
        this.numChannels = numChannels;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
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
