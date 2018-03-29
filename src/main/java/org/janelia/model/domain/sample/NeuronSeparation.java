package org.janelia.model.domain.sample;

import org.janelia.model.domain.ReverseReference;
import org.janelia.model.domain.support.SearchTraversal;

/**
 * The result of running the Neuron Separator on some input file.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class NeuronSeparation extends PipelineResult {

    @SearchTraversal({Sample.class})
    private ReverseReference fragments;

    private String compressionType;

    private Boolean hasWeights = false;

    public ReverseReference getFragmentsReference() {
        return fragments;
    }

    public void setFragmentsReference(ReverseReference fragmentsReference) {
        this.fragments = fragmentsReference;
    }

    public String getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }

    public Boolean getHasWeights() {
        return hasWeights;
    }

    public void setHasWeights(Boolean hasWeights) {
        this.hasWeights = hasWeights;
    }
}
