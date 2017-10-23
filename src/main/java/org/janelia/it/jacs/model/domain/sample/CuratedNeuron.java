package org.janelia.it.jacs.model.domain.sample;

import java.util.ArrayList;
import java.util.List;

import org.janelia.it.jacs.model.domain.Reference;

/**
 * A neuron fragment merged from two primary fragments.  
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class CuratedNeuron extends NeuronFragment {

    private List<Reference> componentFragments = new ArrayList<>();

    public List<Reference> getComponentFragments() {
        return componentFragments;
    }

    public void setComponentFragments(List<Reference> componentFragments) {
        if (componentFragments==null) throw new IllegalArgumentException("Property cannot be null");
        this.componentFragments = componentFragments;
    }
    
}
