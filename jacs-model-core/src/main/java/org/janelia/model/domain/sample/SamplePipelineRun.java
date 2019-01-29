package org.janelia.model.domain.sample;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.janelia.model.access.domain.DomainUtils;
import org.janelia.model.domain.interfaces.HasName;
import org.janelia.model.domain.interfaces.HasResults;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * A single run of the pipeline on an ObjectiveSample. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SamplePipelineRun implements HasResults, HasName, Serializable {

    private Long id;
    private String name;
    private String pipelineProcess;
    private Integer pipelineVersion;
    private Date creationDate;
    private List<PipelineResult> results = new ArrayList<>();
    private PipelineError error;
    private Boolean purged = false;
    private transient ObjectiveSample parent;

    @JsonIgnore
    public ObjectiveSample getParent() {
        return parent;
    }

    @JsonIgnore
    void setParent(ObjectiveSample parent) {
        this.parent = parent;
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
        if (results==null) throw new IllegalArgumentException("Property cannot be null");
        this.results = results;
        for(PipelineResult result : results) {
            result.setParentRun(this);
        }
    }

    @JsonIgnore
    public void addResult(PipelineResult result) {
        result.setParentRun(this);
        results.add(result);
    }

    @JsonIgnore
    public void removeResult(PipelineResult result) {
        results.remove(result);
    }
    
    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <T extends PipelineResult> T getLatestResultOfType(Class<T> resultClass) {
        for (int i = results.size()-1; i>=0; i--) {
            PipelineResult result = results.get(i);
            if (resultClass==null || resultClass.isAssignableFrom(result.getClass())) {
                return (T)result;
            }
        }
        return null;
    }

    @JsonIgnore
    public PipelineResult getLatestResult() {
        return getLatestResultOfType(null);
    }

    @JsonIgnore
    public SampleProcessingResult getLatestProcessingResult() {
        return getLatestResultOfType(SampleProcessingResult.class);
    }

    @JsonIgnore
    public SampleAlignmentResult getLatestAlignmentResult() {
        return getLatestResultOfType(SampleAlignmentResult.class);
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
    public List<LSMSummaryResult> getLSMSummaryResults() {
        return getResultsOfType(LSMSummaryResult.class);
    }
    
    @JsonIgnore
    public List<SampleProcessingResult> getSampleProcessingResults() {
        return getResultsOfType(SampleProcessingResult.class);
    }

    @JsonIgnore
    public List<SamplePostProcessingResult> getSamplePostProcessingResults() {
        return getResultsOfType(SamplePostProcessingResult.class);
    }

    @JsonIgnore
    public List<SampleAlignmentResult> getAlignmentResults() {
        return getResultsOfType(SampleAlignmentResult.class);
    }
    
    public boolean hasError() {
        return error!=null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getPipelineVersion() {
        return pipelineVersion;
    }

    public void setPipelineVersion(Integer pipelineVersion) {
        this.pipelineVersion = pipelineVersion;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
    public String getPipelineProcess() {
        return pipelineProcess;
    }

    public void setPipelineProcess(String pipelineProcess) {
        this.pipelineProcess = pipelineProcess;
    }

    public PipelineError getError() {
        return error;
    }

    public void setError(PipelineError error) {
        this.error = error;
    }

    public void setPurged(Boolean purged) {
        this.purged = purged;
    }

    public Boolean getPurged() {
        return purged;
    }

    @JsonIgnore
    public Long getDiskSpaceUsage() {
        Long total = null;
        for (PipelineResult pipelineResult : results) {
            if (pipelineResult.getDiskSpaceUsage()!=null) {
                if (total==null) {
                    total = pipelineResult.getDiskSpaceUsage();
                }
                else {
                    total += pipelineResult.getDiskSpaceUsage();
                }
            }
        }
        return total;
    }

    @JsonIgnore
    public String getDiskSpaceUsageForHumans() {
        Long diskSpaceUsage = getDiskSpaceUsage();
        return diskSpaceUsage==null ? null : DomainUtils.formatBytesForHumans(diskSpaceUsage);
    }
}
