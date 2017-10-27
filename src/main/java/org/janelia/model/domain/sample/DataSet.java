package org.janelia.model.domain.sample;

import java.util.ArrayList;
import java.util.List;

import org.janelia.model.access.domain.DomainUtils;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchType;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A data set definition which controls how Samples are processed. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="dataSet",label="Data Set")
@SearchType(key="dataSet",label="Data Set")
public class DataSet extends AbstractDomainObject {

    @SearchAttribute(key="identifier_txt",label="Data Set Identifier")
    private String identifier;

    @SearchAttribute(key="sample_name_pattern_txt",label="Sample Name Pattern")
    private String sampleNamePattern;
    
    private String extraStitchBlendParams;

    @SearchAttribute(key="sage_synced_b",label="SAGE Synchronized",facet="sage_synced_b")
    private boolean sageSync;
    
    private boolean neuronSeparationSupported = true;
    
    private boolean basicPostProcessingSupported = false;
    
    private List<String> pipelineProcesses = new ArrayList<>();

    @SearchAttribute(key="sage_config_txt",label="SAGE Config Path")
    private String sageConfigPath;
    
    @SearchAttribute(key="sage_grammar_txt",label="SAGE Grammar Path")
    private String sageGrammarPath;

    @SearchAttribute(key="usage_bytes_l",label="Disk Space Usage (Bytes)")
    private Long diskSpaceUsage;

    private String colorDepthMipPath;
    
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSampleNamePattern() {
        return sampleNamePattern;
    }

    public void setSampleNamePattern(String sampleNamePattern) {
        this.sampleNamePattern = sampleNamePattern;
    }

    public boolean isSageSync() {
        return sageSync;
    }

    public void setSageSync(boolean sageSync) {
        this.sageSync = sageSync;
    }

    public List<String> getPipelineProcesses() {
        return pipelineProcesses;
    }

    public void setPipelineProcesses(List<String> pipelineProcesses) {
        if (pipelineProcesses==null) throw new IllegalArgumentException("Property cannot be null");
        this.pipelineProcesses = pipelineProcesses;
    }

    public String getSageConfigPath() {
        return sageConfigPath;
    }

    public void setSageConfigPath(String sageConfigPath) {
        this.sageConfigPath = sageConfigPath;
    }

    public String getSageGrammarPath() {
        return sageGrammarPath;
    }

    public void setSageGrammarPath(String sageGrammarPath) {
        this.sageGrammarPath = sageGrammarPath;
    }

    public boolean isNeuronSeparationSupported() {
        return neuronSeparationSupported;
    }

    public void setNeuronSeparationSupported(boolean neuronSeparationSupported) {
        this.neuronSeparationSupported = neuronSeparationSupported;
    }

    public String getExtraStitchBlendParams() {
        return extraStitchBlendParams;
    }

    public void setExtraStitchBlendParams(String extraStitchBlendParams) {
        this.extraStitchBlendParams = extraStitchBlendParams;
    }

    public void setBasicPostProcessingSupported(boolean flag) {
        this.basicPostProcessingSupported = flag;
    }
    
    public boolean isBasicPostProcessingSupported() {
        return basicPostProcessingSupported;
    }
    
    public Long getDiskSpaceUsage() {
        return diskSpaceUsage;
    }

    public void setDiskSpaceUsage(Long diskSpaceUsage) {
        this.diskSpaceUsage = diskSpaceUsage;
    }

    @JsonIgnore
    @SearchAttribute(key="usage_humans_t",label="Disk Space Usage")
    public String getDiskSpaceUsageForHumans() {
        return diskSpaceUsage==null ? null : DomainUtils.formatBytesForHumans(diskSpaceUsage);
    }

    public String getColorDepthMipPath() {
        return colorDepthMipPath;
    }

    public void setColorDepthMipPath(String colorDepthMipPath) {
        this.colorDepthMipPath = colorDepthMipPath;
    }
}
