package org.janelia.model.domain.sample;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class CompressionStrategy {

    private String unaligned;

    private String aligned;

    private String separation;

    public String getUnaligned() {
        return unaligned;
    }

    public void setUnaligned(String unaligned) {
        this.unaligned = unaligned;
    }

    public String getAligned() {
        return aligned;
    }

    public void setAligned(String aligned) {
        this.aligned = aligned;
    }

    public String getSeparation() {
        return separation;
    }

    public void setSeparation(String separation) {
        this.separation = separation;
    }
}
