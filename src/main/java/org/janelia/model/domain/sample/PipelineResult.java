package org.janelia.model.domain.sample;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.janelia.model.access.domain.DomainUtils;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.interfaces.HasIdentifier;
import org.janelia.model.domain.interfaces.HasName;
import org.janelia.model.domain.interfaces.HasRelativeFiles;
import org.janelia.model.domain.interfaces.HasResults;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The result of some processing. May be nested if further processing is done on this result.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public class PipelineResult implements HasRelativeFiles, HasIdentifier, HasResults, HasName, Serializable {

    private Long id;
    private String name;
    private String filepath;
    private Long diskSpaceUsage;
    private Date creationDate;
    private List<PipelineResult> results = new ArrayList<>();
    private Map<FileType, String> files = new HashMap<>();
    private Boolean purged = false;
    private String message;
    private Reference containerRef;
    private String containerApp;
    private String compressionType;
    private transient SamplePipelineRun parentRun;
    private transient PipelineResult parentResult;

    @JsonIgnore
    public SamplePipelineRun getParentRun() {
        if (parentRun==null && parentResult!=null) {
            // Populate the parent run, since deserialization won't do it. 
            parentRun = parentResult.getParentRun();
        }
        return parentRun;
    }

    @JsonIgnore
    void setParentRun(SamplePipelineRun parentRun) {
        this.parentRun = parentRun;
    }

    @JsonIgnore
    public PipelineResult getParentResult() {
        return parentResult;
    }

    @JsonIgnore
    void setParentResult(PipelineResult parentResult) {
        this.parentResult = parentResult;
    }

    @JsonIgnore
    public boolean hasResults() {
        return !results.isEmpty();
    }
    
    @JsonProperty
    @Override
    public List<PipelineResult> getResults() {
        return results;
    }
    
    @JsonProperty
    public void setResults(List<PipelineResult> results) {
        for(PipelineResult result : results) {
            result.setParentRun(parentRun);
            result.setParentResult(this);
        }
        this.results = results;
    }

    @JsonIgnore
    public void addResult(PipelineResult result) {
        result.setParentRun(parentRun);
        result.setParentResult(this);
        results.add(result);
    }

    @JsonIgnore
    public void removeResult(PipelineResult result) {
        result.setParentRun(null);
        result.setParentResult(null);
        results.remove(result);
    }
    
    @JsonIgnore
    protected PipelineResult getLatestResultOfType(Class<? extends PipelineResult> type) {
        for (int i = results.size()-1; i>=0; i--) {
            PipelineResult result = results.get(i);
            if (type==null || type.isAssignableFrom(result.getClass())) {
                return result;
            }
        }
        return null;
    }

    @JsonIgnore
    public NeuronSeparation getLatestSeparationResult() {
        return (NeuronSeparation) getLatestResultOfType(NeuronSeparation.class);
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <T extends PipelineResult> List<T> getResultsOfType(Class<T> resultClass) {
        List<T> filteredResults = new ArrayList<>();
        for (PipelineResult result : results) {
            if (resultClass==null || resultClass.isAssignableFrom(result.getClass())) {
                filteredResults.add((T)result);
            }
        }
        return filteredResults;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends PipelineResult> List<T> getResultsById(Class<T> resultClass, Long resultId) {
        List<T> results = new ArrayList<>();
        for(PipelineResult result : getResults()) {
            if (resultId.equals(result.getId()) && (resultClass==null || resultClass.isAssignableFrom(result.getClass()))) {
                results.add((T)result);
            }
            for(T childResult : result.getResultsById(resultClass, resultId)) {
                results.add(childResult);
            }
        }
        return results;
    }

    @JsonIgnore
    public String getDiskSpaceUsageForHumans() {
        return diskSpaceUsage==null ? null : DomainUtils.formatBytesForHumans(diskSpaceUsage);
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public Long getDiskSpaceUsage() {
        return diskSpaceUsage;
    }

    public void setDiskSpaceUsage(Long diskSpaceUsage) {
        this.diskSpaceUsage = diskSpaceUsage;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Map<FileType, String> getFiles() {
        return files;
    }

    public void setFiles(Map<FileType, String> files) {
        if (files==null) throw new IllegalArgumentException("Property cannot be null");
        this.files = files;
    }

    public Boolean getPurged() {
        return purged;
    }

    public void setPurged(Boolean purged) {
        this.purged = purged;
    }

    /**
     * Returns the error messages for any non-fatal errors that occured during processing of this result.
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Reference getContainerRef() {
        return containerRef;
    }

    public void setContainerRef(Reference containerRef) {
        this.containerRef = containerRef;
    }

    public String getContainerApp() {
        return containerApp;
    }

    public void setContainerApp(String containerApp) {
        this.containerApp = containerApp;
    }

    public String getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }
}
