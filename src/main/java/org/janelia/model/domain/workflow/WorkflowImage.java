package org.janelia.model.domain.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.interfaces.HasAnatomicalArea;
import org.janelia.model.domain.interfaces.HasImageStack;
import org.janelia.model.domain.interfaces.HasRelativeFiles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public class WorkflowImage extends AbstractDomainObject implements HasAnatomicalArea, HasImageStack, HasRelativeFiles  {

    private String filepath;
    private String imageSize;
    private String opticalResolution;
    private String anatomicalArea;
    private String tile;
    private String channelSpec;
    private String channelColors;
    private Map<FileType, String> files = new HashMap<>();
    private Set<FileType> deleteOnExit = new HashSet<>();

    /** Empty constructor for deserialization */
    public WorkflowImage() {
    }

    /** Copy constructor */
    public WorkflowImage(WorkflowImage image) {
        this.filepath = image.getFilepath();
        this.imageSize = image.getImageSize();
        this.opticalResolution = image.getOpticalResolution();
        this.anatomicalArea = image.getAnatomicalArea();
        this.tile = image.getTile();
        this.channelSpec = image.getChannelSpec();
        this.channelColors = image.getChannelColors();
        this.files = new HashMap<>(image.getFiles());
        this.deleteOnExit = new HashSet<>(image.getDeleteOnExit());
    }

    @Override
    public String getFilepath() {
        return filepath;
    }

    @Override
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    @Override
    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public String getOpticalResolution() {
        return opticalResolution;
    }

    public void setOpticalResolution(String opticalResolution) {
        this.opticalResolution = opticalResolution;
    }

    @Override
    public String getAnatomicalArea() {
        return anatomicalArea;
    }

    public void setAnatomicalArea(String anatomicalArea) {
        this.anatomicalArea = anatomicalArea;
    }

    public String getTile() {
        return tile;
    }

    public void setTile(String tile) {
        this.tile = tile;
    }

    @Override
    public String getChannelSpec() {
        return channelSpec;
    }

    public void setChannelSpec(String channelSpec) {
        this.channelSpec = channelSpec;
    }

    @Override
    public String getChannelColors() {
        return channelColors;
    }

    public void setChannelColors(String channelColors) {
        this.channelColors = channelColors;
    }

    @Override
    public Map<FileType, String> getFiles() {
        return files;
    }

    public void setFiles(Map<FileType, String> files) {
        if (files==null) throw new IllegalArgumentException("Property cannot be null");
        this.files = files;
    }

    public Set<FileType> getDeleteOnExit() {
        return deleteOnExit;
    }

    public void setDeleteOnExit(Set<FileType> deleteOnExit) {
        if (deleteOnExit==null) throw new IllegalArgumentException("Property cannot be null");
        this.deleteOnExit = deleteOnExit;
    }

    @JsonIgnore
    public void setDeleteOnExit(FileType... deleteOnExits) {
        deleteOnExit.clear();
        for (FileType fileType : deleteOnExits) {
            deleteOnExit.add(fileType);
        }

    }
}
