package org.janelia.model.domain.sample;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Sets;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.DomainUtils;
import org.janelia.model.domain.gui.search.Filtering;
import org.janelia.model.domain.gui.search.criteria.Criteria;
import org.janelia.model.domain.gui.search.criteria.FacetCriteria;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchType;

import java.util.ArrayList;
import java.util.List;

/**
 * A data set definition which controls how Samples are processed. 
 *
 * The color depth counts in this class are for legacy clients.
 * See the corresponding ColorDepthLibrary for the current implementation.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="dataSet",label="Data Set")
@SearchType(key="dataSet",label="Data Set")
public class DataSet extends AbstractDomainObject implements Filtering {

    @SearchAttribute(key="identifier_txt",label="Data Set Identifier")
    private String identifier;

    @SearchAttribute(key="sample_name_pattern_txt",label="Sample Name Pattern")
    private String sampleNamePattern;

    @SearchAttribute(key="sage_synced_b",label="SAGE Synchronized",facet="sage_synced_b")
    private boolean sageSync;

    private boolean distortionCorrectionSupported = true;

    private boolean neuronSeparationSupported = true;
    
    private boolean basicPostProcessingSupported = false;
    
    private List<String> pipelineProcesses = new ArrayList<>();

    @SearchAttribute(key="sage_config_txt",label="SAGE Config Path")
    private String sageConfigPath;
    
    @SearchAttribute(key="sage_grammar_txt",label="SAGE Grammar Path")
    private String sageGrammarPath;

    @SearchAttribute(key="usage_bytes_l",label="Disk Space Usage (Bytes)")
    private Long diskSpaceUsage;

    private CompressionStrategy compressionStrategy;


    @JsonIgnore
    private List<Criteria> lazyCriteria;

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

    public boolean isDistortionCorrectionSupported() {
        return distortionCorrectionSupported;
    }

    public void setDistortionCorrectionSupported(boolean distortionCorrectionSupported) {
        this.distortionCorrectionSupported = distortionCorrectionSupported;
    }

    public boolean isNeuronSeparationSupported() {
        return neuronSeparationSupported;
    }

    public void setNeuronSeparationSupported(boolean neuronSeparationSupported) {
        this.neuronSeparationSupported = neuronSeparationSupported;
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

    public CompressionStrategy getCompressionStrategy() {
        return compressionStrategy;
    }

    public void setCompressionStrategy(CompressionStrategy compressionStrategy) {
        this.compressionStrategy = compressionStrategy;
    }

    @JsonIgnore
    @SearchAttribute(key="comp_txt",label="Compression for Unaligned Stacks")
    public String getUnalignedCompressionType() {
        if (compressionStrategy==null) return null;
        return compressionStrategy.getUnaligned();
    }

    @JsonIgnore
    @SearchAttribute(key="comp_txt",label="Compression for Aligned Stacks")
    public String getAlignedCompressionType() {
        if (compressionStrategy==null) return null;
        return compressionStrategy.getAligned();
    }

    @JsonIgnore
    @SearchAttribute(key="ncomp_txt",label="Compression for Separations")
    public String getSeparationCompressionType() {
        if (compressionStrategy==null) return null;
        return compressionStrategy.getSeparation();
    }

    @JsonIgnore
    public void setUnalignedCompressionType(String compressionType) {
        if (compressionStrategy==null) {
            this.compressionStrategy = new CompressionStrategy();
        }
        compressionStrategy.setUnaligned(compressionType);
    }

    @JsonIgnore
    public void setAlignedCompressionType(String compressionType) {
        if (compressionStrategy==null) {
            this.compressionStrategy = new CompressionStrategy();
        }
        compressionStrategy.setAligned(compressionType);
    }

    @JsonIgnore
    public void setSeparationCompressionType(String compressionType) {
        if (compressionStrategy==null) {
            this.compressionStrategy = new CompressionStrategy();
        }
        compressionStrategy.setSeparation(compressionType);
    }

    /* implement Filtering interface */

    @JsonIgnore
    @Override
    public String getSearchClass() {
        return Sample.class.getName();
    }

    @JsonIgnore
    @Override
    public boolean hasCriteria() {
        return true;
    }

    @JsonIgnore
    @Override
    public String getSearchString() {
        return null;
    }

    @JsonIgnore
    @Override
    public List<Criteria> getCriteriaList() {
        if (lazyCriteria==null) {
            lazyCriteria = new ArrayList<>();
            FacetCriteria sageSynced = new FacetCriteria();
            sageSynced.setAttributeName("sageSynced");
            sageSynced.setValues(Sets.newHashSet("true"));
            lazyCriteria.add(sageSynced);
            FacetCriteria dataSet = new FacetCriteria();
            dataSet.setAttributeName("dataSet");
            dataSet.getValues().add(getIdentifier());
            lazyCriteria.add(dataSet);
        }
        return lazyCriteria;
    }

}
