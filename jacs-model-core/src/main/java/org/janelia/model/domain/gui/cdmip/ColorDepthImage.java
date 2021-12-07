package org.janelia.model.domain.gui.cdmip;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.interfaces.HasAnatomicalArea;
import org.janelia.model.domain.sample.AlignedImage2d;
import org.janelia.model.domain.sample.Sample;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.support.SearchType;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="cdmipImage",label="Color Depth Image")
@SearchType(key="cdmipImage",label="Color Depth Image")
public class ColorDepthImage extends AlignedImage2d implements HasAnatomicalArea {

    @SearchAttribute(key="library_sm",label="Color Depth Library", facet="library_sm")
    private Set<String> libraries = new HashSet<>();

    private String anatomicalArea;

    @SearchTraversal({})
    private Reference sampleRef;

    private int channelNumber;

    @SearchTraversal({})
    private Reference emBodyRef;

    // Denormalized from EMBody
    @SearchAttribute(key="bodyid_l",label="Body Id")
    private Long bodyId;

    // Denormalized from EMBody
    @SearchAttribute(key="ntype_txt",label="Neuron Cell Type")
    private String neuronType;

    // Denormalized from EMBody
    @SearchAttribute(key="ninstance_txt",label="Neuron Instance")
    private String neuronInstance;

    // Denormalized from EMBody
    @SearchAttribute(key="status_txt",label="Status",facet="status_s")
    private String neuronStatus;

    private String publicImageUrl;

    private String publicThumbnailUrl;

    @SearchTraversal({})
    private Reference sourceImageRef;

    @SearchTraversal({})
    @JsonIgnore
    private Sample sample;

    public Set<String> getLibraries() {
        return libraries;
    }

    public void setLibraries(Set<String> libraries) {
        if (libraries==null) throw new IllegalArgumentException("Property cannot be null");
        this.libraries = libraries;
    }

    public Reference getSampleRef() {
        return sampleRef;
    }

    public void setSampleRef(Reference sampleRef) {
        this.sampleRef = sampleRef;
    }

    public Reference getEmBodyRef() {
        return emBodyRef;
    }

    public void setEmBodyRef(Reference emBodyRef) {
        this.emBodyRef = emBodyRef;
    }

    public Reference getSourceImageRef() {
        return sourceImageRef;
    }

    public void setSourceImageRef(Reference sourceImageRef) {
        this.sourceImageRef = sourceImageRef;
    }

    @Override
    public String getAnatomicalArea() {
        return anatomicalArea;
    }

    public void setAnatomicalArea(String anatomicalArea) {
        this.anatomicalArea = anatomicalArea;
    }

    public int getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    @JsonIgnore
    public Map<FileType, String> getFiles() {
        return ImmutableMap.of(FileType.Unclassified2d, getFilepath());
    }

    @Override
    public void setFiles(Map<FileType, String> files) {
        throw new UnsupportedOperationException("This field is calculated from filepath and cannot be changed");
    }

    public String getPublicImageUrl() {
        return publicImageUrl;
    }

    public void setPublicImageUrl(String publicImageUrl) {
        this.publicImageUrl = publicImageUrl;
    }

    public String getPublicThumbnailUrl() {
        return publicThumbnailUrl;
    }

    public void setPublicThumbnailUrl(String publicThumbnailUrl) {
        this.publicThumbnailUrl = publicThumbnailUrl;
    }

    public Long getBodyId() {
        return bodyId;
    }

    public void setBodyId(Long bodyId) {
        this.bodyId = bodyId;
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

    public String getNeuronStatus() {
        return neuronStatus;
    }

    public void setNeuronStatus(String neuronStatus) {
        this.neuronStatus = neuronStatus;
    }

    @JsonProperty
    public Sample getSample() {
        return sample;
    }

    @JsonIgnore
    void setSample(Sample sample) {
        this.sample = sample;
    }
}
