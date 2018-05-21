package org.janelia.model.domain.workflow;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.enums.algorithms.AlignmentAlgorithm;
import org.janelia.model.domain.enums.algorithms.MergeAlgorithm;
import org.janelia.model.domain.enums.algorithms.PostAlgorithm;
import org.janelia.model.domain.enums.algorithms.StitchAlgorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration which drives the Sample pipeline.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SamplePipelineConfiguration extends AbstractDomainObject {

    private String identifier;
    private boolean distortionCorrection = true;
    private MergeAlgorithm mergeAlgorithm = MergeAlgorithm.FLYLIGHT_ORDERED;
    private StitchAlgorithm stitchAlgorithm = StitchAlgorithm.FLYLIGHT;
    private PostAlgorithm postAlgorithm = PostAlgorithm.ASO;
    private String channelDyeSpec;
    private String outputChannelOrder;
    private String outputColorSpec;
    private List<AlignmentAlgorithm> alignments = new ArrayList<>();
    private boolean neuronSeparation = false;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isDistortionCorrection() {
        return distortionCorrection;
    }

    public void setDistortionCorrection(boolean distortionCorrection) {
        this.distortionCorrection = distortionCorrection;
    }

    public MergeAlgorithm getMergeAlgorithm() {
        return mergeAlgorithm;
    }

    public void setMergeAlgorithm(MergeAlgorithm mergeAlgorithm) {
        this.mergeAlgorithm = mergeAlgorithm;
    }

    public StitchAlgorithm getStitchAlgorithm() {
        return stitchAlgorithm;
    }

    public void setStitchAlgorithm(StitchAlgorithm stitchAlgorithm) {
        this.stitchAlgorithm = stitchAlgorithm;
    }

    public PostAlgorithm getPostAlgorithm() {
        return postAlgorithm;
    }

    public void setPostAlgorithm(PostAlgorithm postAlgorithm) {
        this.postAlgorithm = postAlgorithm;
    }

    public String getChannelDyeSpec() {
        return channelDyeSpec;
    }

    public void setChannelDyeSpec(String channelDyeSpec) {
        this.channelDyeSpec = channelDyeSpec;
    }

    public String getOutputChannelOrder() {
        return outputChannelOrder;
    }

    public void setOutputChannelOrder(String outputChannelOrder) {
        this.outputChannelOrder = outputChannelOrder;
    }

    public String getOutputColorSpec() {
        return outputColorSpec;
    }

    public void setOutputColorSpec(String outputColorSpec) {
        this.outputColorSpec = outputColorSpec;
    }

    public List<AlignmentAlgorithm> getAlignments() {
        return alignments;
    }

    public void setAlignments(List<AlignmentAlgorithm> alignments) {
        this.alignments = alignments;
    }

    public boolean isNeuronSeparation() {
        return neuronSeparation;
    }

    public void setNeuronSeparation(boolean neuronSeparation) {
        this.neuronSeparation = neuronSeparation;
    }
}