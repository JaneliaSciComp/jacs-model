package org.janelia.it.jacs.model.domain.sample;

import org.janelia.it.jacs.model.domain.ReverseReference;
import org.janelia.it.jacs.model.domain.support.SearchTraversal;

/**
 * The result of running the Neuron Separator on some input file.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class NeuronSeparation extends PipelineResult {

    @SearchTraversal({Sample.class})
    private ReverseReference fragments;

    private Boolean hasWeights = false;

    public ReverseReference getFragmentsReference() {
        return fragments;
    }

    public void setFragmentsReference(ReverseReference fragmentsReference) {
        this.fragments = fragmentsReference;
    }

    public Boolean getHasWeights() {
        return hasWeights;
    }

    public void setHasWeights(Boolean hasWeights) {
        this.hasWeights = hasWeights;
    }
}
