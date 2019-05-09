package org.janelia.model.domain.tiledMicroscope;

import java.util.List;

import org.janelia.model.domain.DomainUtils;

/**
 * DTO for a bulk update to many neurons at once.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class BulkNeuronStyleUpdate {
    
    private List<Long> neuronIds;
    private Boolean visible;
    private String colorHex;

    public BulkNeuronStyleUpdate() {
    }

    public List<Long> getNeuronIds() {
        return neuronIds;
    }

    public void setNeuronIds(List<Long> neuronIds) {
        this.neuronIds = neuronIds;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    @Override
    public String toString() {
        return "BulkNeuronStyleUpdate[neuronIds=" + DomainUtils.abbr(neuronIds) + 
                ", visible=" + visible + 
                ", colorHex=" + colorHex + "]";
    }
}
