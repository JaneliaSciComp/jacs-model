package org.janelia.model.domain.flyem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.interfaces.HasFiles;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.support.SearchType;

/**
 * Neuron body loaded from FlyEM's neuPrint.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="emBody",label="EM Body")
@SearchType(key="emBody",label="EM Body")
public class EMBody extends AbstractDomainObject implements HasFiles {

    private Reference dataSetRef;

    @SearchAttribute(key="dataset_txt",label="Data Set",facet="data_set_s")
    private String dataSetIdentifier;

    @SearchAttribute(key="status_txt",label="Status",facet="status_s")
    private String status;

    @SearchAttribute(key="ntype_txt",label="Neuron Cell Type")
    private String neuronType;

    @SearchAttribute(key="ninstance_txt",label="Neuron Instance")
    private String neuronInstance;

    private List<String> terms;

    @SearchAttribute(key="voxel_l",label="Num Voxels")
    private Long voxelSize;

    @SearchAttribute(key="status_txt",label="Status Label")
    private String statusLabel;

    // emDataSet property is serializable but not deserializable
    @SearchTraversal({})
    @JsonIgnore
    private EMDataSet emDataSet;

    private Map<FileType, String> files = new HashMap<>();

    public Reference getDataSetRef() {
        return dataSetRef;
    }

    public void setDataSetRef(Reference dataSetRef) {
        this.dataSetRef = dataSetRef;
    }

    public String getDataSetIdentifier() {
        return dataSetIdentifier;
    }

    public void setDataSetIdentifier(String dataSetIdentifier) {
        this.dataSetIdentifier = dataSetIdentifier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNeuronType() {
        return neuronType;
    }

    public void setNeuronType(String neuronType) {
        this.neuronType = neuronType;
    }

    public String getNeuronInstance() {
        return neuronInstance;
    }

    public void setNeuronInstance(String neuronInstance) {
        this.neuronInstance = neuronInstance;
    }

    public List<String> getTerms() {
        return terms;
    }

    public void setTerms(List<String> terms) {
        this.terms = terms;
    }

    public Long getVoxelSize() {
        return voxelSize;
    }

    public void setVoxelSize(Long voxelSize) {
        this.voxelSize = voxelSize;
    }

    @Override
    public Map<FileType, String> getFiles() {
        return files;
    }

    public void setFiles(Map<FileType, String> files) {
        if (files==null) throw new IllegalArgumentException("Property cannot be null");
        this.files = files;
    }

    @JsonIgnore
    public Long getBodyId() {
        return new Long(getName());
    }

    @JsonIgnore
    public void setBodyId(Long bodyId) {
        setName(bodyId.toString());
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    @JsonProperty
    public EMDataSet getEmDataSet() {
        return emDataSet;
    }

    @JsonIgnore
    public void setEmDataSet(EMDataSet emDataSet) {
        this.emDataSet = emDataSet;
    }
}
