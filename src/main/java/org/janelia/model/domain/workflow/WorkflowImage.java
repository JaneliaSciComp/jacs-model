package org.janelia.model.domain.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.interfaces.HasAnatomicalArea;
import org.janelia.model.domain.interfaces.HasFiles;
import org.janelia.model.domain.interfaces.HasImageStack;
import org.janelia.model.domain.sample.LSMImage;
import org.janelia.model.domain.support.SearchTraversal;

import java.util.*;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public class WorkflowImage extends AbstractDomainObject implements HasAnatomicalArea, HasImageStack, HasFiles {

    private static final String TILE_STITCHED = "stitched";

    @SearchTraversal({})
    private Reference sampleRef;

    private String objective;
    private String imageSize;
    private String opticalResolution;

    // Used for stitching
    private String anatomicalArea;
    private String tile;

    // Used for normalization
    private String channelSpec;
    private String channelColors;
    private String channelDyeNames;

    // Used for distortion correction
    private String microscope;
    private Date captureDate;

    // Used for alignment
    private String gender;

    // For related files on disk
    private Map<FileType, String> files = new HashMap<>();

    // Temporary files which can be deleted when the pipeline is finished
    private Set<FileType> deleteOnExit = new HashSet<>();

    /** Empty constructor for deserialization */
    public WorkflowImage() {
    }

    /** Copy constructor */
    public WorkflowImage(WorkflowImage image) {
        this.setName(image.getName());
        this.setOwnerKey(image.getOwnerKey());
        this.sampleRef = image.getSample();
        this.objective = image.getObjective();
        this.imageSize = image.getImageSize();
        this.opticalResolution = image.getOpticalResolution();
        this.anatomicalArea = image.getAnatomicalArea();
        this.tile = image.getTile();
        this.channelSpec = image.getChannelSpec();
        this.channelColors = image.getChannelColors();
        this.microscope = image.getMicroscope();
        this.captureDate = image.getCaptureDate();
        this.gender = image.getGender();
        this.files = new HashMap<>(image.getFiles());
        this.deleteOnExit = new HashSet<>(image.getDeleteOnExit());
    }

    /** Copy constructor */
    public WorkflowImage(LSMImage lsm) {
        this.setName(lsm.getName());
        this.setOwnerKey(lsm.getOwnerKey());
        this.sampleRef = lsm.getSample();
        this.objective = lsm.getObjective();
        this.imageSize = lsm.getImageSize();
        this.opticalResolution = lsm.getOpticalResolution();
        this.anatomicalArea = lsm.getAnatomicalArea();
        this.tile = lsm.getTile();
        this.channelSpec = lsm.getChanSpec();
        this.channelColors = lsm.getChannelColors();
        this.channelDyeNames = lsm.getChannelDyeNames();
        this.microscope = lsm.getMicroscope();
        this.captureDate = lsm.getCaptureDate();
        this.gender = lsm.getGender();
        this.files = new HashMap<>(lsm.getFiles());
        files.put(FileType.LosslessStack, lsm.getFilepath());
    }

    public boolean isStitched() {
        return TILE_STITCHED.equals(tile);
    }

    public Reference getSample() {
        return sampleRef;
    }

    public void setSample(Reference sampleRef) {
        this.sampleRef = sampleRef;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getMicroscope() {
        return microscope;
    }

    public void setMicroscope(String microscope) {
        this.microscope = microscope;
    }

    public Date getCaptureDate() {
        return captureDate;
    }

    public void setCaptureDate(Date captureDate) {
        this.captureDate = captureDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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

    public String getChannelDyeNames() {
        return channelDyeNames;
    }

    public void setChannelDyeNames(String channelDyeNames) {
        this.channelDyeNames = channelDyeNames;
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
