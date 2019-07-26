package org.janelia.model.domain.sample;

import org.janelia.model.domain.support.ReprocessOnChange;
import org.janelia.model.domain.support.SAGEAttribute;
import org.janelia.model.domain.support.SearchAttribute;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class Image3d extends Image {

    @SAGEAttribute(cvName="light_imagery", termName="channels")
    @SearchAttribute(key="num_channels_i",label="Num Channels", facet="num_channels_i")
    private Integer numChannels;

    @ReprocessOnChange
    @SAGEAttribute(cvName="light_imagery", termName="channel_spec")
    @SearchAttribute(key="chanspec_txt",label="Channel Specification",facet="chanspec_s")
    private String chanSpec;

    public Integer getNumChannels() {
        return numChannels;
    }

    public void setNumChannels(Integer numChannels) {
        this.numChannels = numChannels;
    }

    public String getChanSpec() {
        return chanSpec;
    }

    public void setChanSpec(String chanSpec) {
        this.chanSpec = chanSpec;
    }
}
