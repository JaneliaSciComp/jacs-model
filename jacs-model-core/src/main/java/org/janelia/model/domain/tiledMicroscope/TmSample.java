package org.janelia.model.domain.tiledMicroscope;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.files.SyncedPath;
import org.janelia.model.domain.interfaces.HasFiles;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchType;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tiled microscope sample.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@SearchType(key="tmSample",label="Horta Sample")
@MongoMapped(collectionName="tmSample",label="Horta Sample")
public class TmSample extends SyncedPath implements HasFiles {

    private Map<FileType, String> files = new HashMap<>();

    @SearchAttribute(key="micron_to_vox_txt",label="Micron to Voxel Matrix")
    private String micronToVoxMatrix;
    
    @SearchAttribute(key="vox_to_micron_txt",label="Voxel to Micron Matrix")
    private String voxToMicronMatrix;

    private List<Integer> origin;
    private List<Double> scaling;
    private Long numImageryLevels;

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
    public boolean hasCompressedAcquisition() {
        return files.containsKey(FileType.CompressedAcquisition);
    }

    @JsonIgnore
    @SearchAttribute(key="path_raw_txt",label="RAW Filepath")
    public String getAcquisitionFilepath() {
        if (files.containsKey(FileType.CompressedAcquisition)) {
            return files.get(FileType.CompressedAcquisition);
        }
        return files.get(FileType.TwoPhotonAcquisition);
    }

    @JsonIgnore
    public String setLargeVolumeOctreeFilepath(String filepath) {
        super.setFilepath(filepath);
        return files.put(FileType.LargeVolumeOctree, filepath);
    }

    @JsonIgnore
    public String setLargeVolumeKTXFilepath(String filepath) {
        return files.put(FileType.LargeVolumeKTX, filepath);
    }

    @JsonIgnore
    public String setAcquisitionFilepath(String filepath, boolean compressed) {
        if (compressed) {
            files.remove(FileType.TwoPhotonAcquisition);
            return files.put(FileType.CompressedAcquisition, filepath);
        } else {
            files.remove(FileType.CompressedAcquisition);
            return files.put(FileType.TwoPhotonAcquisition, filepath);
        }
    }

    @Override
    public String getFilepath() {
        // enforce filepath==LargeVolumeOctree invariant
        String filepath = super.getFilepath();
        if (filepath==null || !filepath.equals(getLargeVolumeOctreeFilepath())) {
            super.setFilepath(getLargeVolumeOctreeFilepath());
        }
        return super.getFilepath();
    }

    @Override
    public void setFilepath(String filepath) {
        // enforce filepath==LargeVolumeOctree invariant
        setLargeVolumeOctreeFilepath(filepath);
    }

    /**
     * Use DomainUtils.getFilepath instead of this method, to get the absolute path. This method is only here to support serialization.
     */
    @Override
    public Map<FileType, String> getFiles() {
        return files;
    }

    /**
     * Use DomainUtils.setFilepath instead of this method, to set the absolute path. This method is only here to support serialization.
     */
    public void setFiles(Map<FileType, String> files) {
        if (files==null) throw new IllegalArgumentException("Property cannot be null");
        this.files = files;
    }

    /**
     * @deprecated Use isExistsInStorage instead.
     */
    @Deprecated
    public boolean isFilesystemSync() {
        return isExistsInStorage();
    }

    /**
     * @deprecated Use setExistsInStorage instead.
     */
    @Deprecated
    public void setFilesystemSync(boolean filesystemSync) {
        setExistsInStorage(filesystemSync);
    }
}
