package org.janelia.model.domain.sample;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.interfaces.HasFiles;
import org.janelia.model.domain.interfaces.HasName;
import org.janelia.model.domain.support.SearchTraversal;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A sample tile consists of a set of LSMs with the same objective, 
 * and in the same anatomical area. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleTile implements HasFiles, HasName, Serializable {

    private String name;
    private String anatomicalArea;
    @SearchTraversal({Sample.class})
    private List<Reference> lsmReferences;
    private Map<FileType, String> files = new HashMap<>();
    private transient ObjectiveSample parent;
    private Boolean blockAreaImageCreation = null;
    private Boolean blockAnatomicalAreaCreation = null;
    private Boolean blockNeuronSeparation = null;

    public void setBlockAreaImageCreation(Boolean flag) {
        blockAreaImageCreation = flag;
    }

    public void setBlockAnatomicalAreaCreation(Boolean flag) {
        blockAnatomicalAreaCreation = flag;
    }

    public Boolean isBlockAreaImageCreation() {
        return blockAreaImageCreation == null ? Boolean.FALSE : blockAreaImageCreation;
    }

    public Boolean isBlockAnatomicalAreaCreation() {
        return blockAnatomicalAreaCreation == null ? Boolean.FALSE : blockAnatomicalAreaCreation;
    }

    public void setBlockNeuronSeparation(Boolean flag) { blockNeuronSeparation = flag; }

    public Boolean isBlockNeuronSeparation() {
        return blockNeuronSeparation == null ? Boolean.FALSE : blockNeuronSeparation;
    }

    @JsonIgnore
    public ObjectiveSample getParent() {
        return parent;
    }

    @JsonIgnore
    void setParent(ObjectiveSample parent) {
        this.parent = parent;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAnatomicalArea() {
        return anatomicalArea;
    }

    public void setAnatomicalArea(String anatomicalArea) {
        this.anatomicalArea = anatomicalArea;
    }

    public List<Reference> getLsmReferences() {
        return lsmReferences;
    }

    public void setLsmReferences(List<Reference> lsmReferences) {
        this.lsmReferences = lsmReferences;
    }

    public Map<FileType, String> getFiles() {
        return files;
    }

    public void setFiles(Map<FileType, String> files) {
        if (files==null) throw new IllegalArgumentException("Property cannot be null");
        this.files = files;
    }

}
