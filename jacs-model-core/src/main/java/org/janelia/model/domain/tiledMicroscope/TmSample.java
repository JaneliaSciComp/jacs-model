package org.janelia.model.domain.tiledMicroscope;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.interfaces.HasFiles;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchType;

/**
 * Tiled microscope sample.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@SearchType(key="tmSample",label="Tiled Microscope Sample")
@MongoMapped(collectionName="tmSample",label="Tiled Microscope Sample")
public class TmSample extends AbstractDomainObject implements HasFiles {

    @SearchAttribute(key="micron_to_vox_txt",label="Micron to Voxel Matrix")
    private String micronToVoxMatrix;
    
    @SearchAttribute(key="vox_to_micron_txt",label="Voxel to Micron Matrix")
    private String voxToMicronMatrix;

    private List<Integer> origin;
    private List<Double> scaling;
    private Long numImageryLevels;

    private Map<FileType, String> files = new HashMap<>();

    /** This flag is set to false when a desync is detected, meaning one or more of the paths in files cannot be found */
    private boolean filesystemSync = true;

    /** Legacy path, now  */
    @Deprecated
    private String filepath;

    public TmSample() {
    }

    public TmSample(Long id, String name) {
        setId(id);
        setName(name);
    }

    public TmSample(Long id, String name, Date creationDate) {
        this(id, name);
        setCreationDate(creationDate);
    }

    public String getVoxToMicronMatrix() {
        return voxToMicronMatrix;
    }

    public void setVoxToMicronMatrix(String voxToMicronMatrix) {
        this.voxToMicronMatrix = voxToMicronMatrix;
    }

    public String getMicronToVoxMatrix() {
        return micronToVoxMatrix;
    }

    public void setMicronToVoxMatrix(String micronToVoxMatrix) {
        this.micronToVoxMatrix = micronToVoxMatrix;
    }

    public Long getNumImageryLevels() {
        return numImageryLevels;
    }

    public void setNumImageryLevels(Long numImageryLevels) {
        this.numImageryLevels = numImageryLevels;
    }

    public List<Integer> getOrigin() {
        return origin;
    }

    public void setOrigin(List<Integer> origin) {
        this.origin = origin;
    }

    public List<Double> getScaling() {
        return scaling;
    }

    public void setScaling(List<Double> scaling) {
        this.scaling = scaling;
    }

    @JsonIgnore
    @SearchAttribute(key="path_octree_txt",label="Octree Filepath")
    public String getLargeVolumeOctreeFilepath() {
        return files.get(FileType.LargeVolumeOctree);
    }

    @JsonIgnore
    @SearchAttribute(key="path_ktx_txt",label="KTX Filepath")
    public String getLargeVolumeKTXFilepath() {
        return files.get(FileType.LargeVolumeKTX);
    }

    @JsonIgnore
    @SearchAttribute(key="path_raw_txt",label="RAW Filepath")
    public String getTwoPhotonAcquisitionFilepath() {
        return files.get(FileType.TwoPhotonAcquisition);
    }

    @JsonIgnore
    public String setLargeVolumeOctreeFilepath(String filepath) {
        return files.put(FileType.LargeVolumeOctree, filepath);
    }

    @JsonIgnore
    public String setLargeVolumeKTXFilepath(String filepath) {
        return files.put(FileType.LargeVolumeKTX, filepath);
    }

    @JsonIgnore
    public String setTwoPhotonAcquisitionFilepath(String filepath) {
        return files.put(FileType.TwoPhotonAcquisition, filepath);
    }

    /**
     * Use DomainUtils.getFilepath instead of this method, to get the absolute path. This method is only here to support serialization.
     */
    @Override
    public Map<FileType, String> getFiles() {
        return files;
    }

    public void setFiles(Map<FileType, String> files) {
        if (files==null) throw new IllegalArgumentException("Property cannot be null");
        this.files = files;
    }

    public boolean isFilesystemSync() {
        return filesystemSync;
    }

    public void setFilesystemSync(boolean filesystemSync) {
        this.filesystemSync = filesystemSync;
    }

    /**
     * @deprecated replaced by getLargeVolumeOctreeFilepath
     */
    @Deprecated
    public String getFilepath() {
        return filepath;
    }

    /**
     * @deprecated replaced by setLargeVolumeOctreeFilepath
     */
    @Deprecated
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }
}
