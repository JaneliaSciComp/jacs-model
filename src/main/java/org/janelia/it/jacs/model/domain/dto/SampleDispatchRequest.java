package org.janelia.it.jacs.model.domain.dto;

import java.util.List;

import org.janelia.it.jacs.model.domain.Reference;

/**
 * Parameter object for a web service sample dispatch.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleDispatchRequest {

    private List<Reference> sampleReferences;
    private String processLabel;
    private Boolean reuse;
    
    public SampleDispatchRequest() {
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

    public Boolean getReuse() {
        return reuse;
    }

    public void setReuse(Boolean reuse) {
        this.reuse = reuse;
    }
    
    
    
}
