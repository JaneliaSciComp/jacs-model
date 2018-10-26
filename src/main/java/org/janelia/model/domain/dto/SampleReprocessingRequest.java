package org.janelia.model.domain.dto;

import org.janelia.model.domain.Reference;

import java.util.List;

/**
 * Parameter object for a web service sample dispatch.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleReprocessingRequest {

    private List<Reference> sampleReferences;
    private String processLabel;
    private Boolean reuseSummary;
    private Boolean reuseProcessing;
    private Boolean reusePost;
    private Boolean reuseAlignment;
    private Boolean keepExistingResults;
    private String extraOptions;

    public SampleReprocessingRequest() {
    }

    public List<Reference> getSampleReferences() {
        return sampleReferences;
    }

    public void setSampleReferences(List<Reference> sampleReferences) {
        this.sampleReferences = sampleReferences;
    }

    public String getProcessLabel() {
        return processLabel;
    }

    public void setProcessLabel(String processLabel) {
        this.processLabel = processLabel;
    }

    public Boolean getReuseSummary() {
        return reuseSummary;
    }

    public void setReuseSummary(Boolean reuseSummary) {
        this.reuseSummary = reuseSummary;
    }

    public Boolean getReuseProcessing() {
        return reuseProcessing;
    }

    public void setReuseProcessing(Boolean reuseProcessing) {
        this.reuseProcessing = reuseProcessing;
    }

    public Boolean getReusePost() {
        return reusePost;
    }

    public void setReusePost(Boolean reusePost) {
        this.reusePost = reusePost;
    }

    public Boolean getReuseAlignment() {
        return reuseAlignment;
    }

    public void setReuseAlignment(Boolean reuseAlignment) {
        this.reuseAlignment = reuseAlignment;
    }

    public Boolean getKeepExistingResults() {
        return keepExistingResults;
    }

    public void setKeepExistingResults(Boolean keepExistingResults) {
        this.keepExistingResults = keepExistingResults;
    }

    public String getExtraOptions() {
        return extraOptions;
    }

    public void setExtraOptions(String extraOptions) {
        this.extraOptions = extraOptions;
    }
}
