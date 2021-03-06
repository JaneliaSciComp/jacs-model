package org.janelia.model.domain.sample;

import java.util.HashMap;
import java.util.Map;

import org.janelia.model.domain.enums.AlignmentScoreType;
import org.janelia.model.domain.interfaces.HasAnatomicalArea;
import org.janelia.model.domain.interfaces.HasImageStack;
import org.janelia.model.domain.interfaces.IsAligned;

/**
 * The result of running an alignment algorithm on a sample. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleAlignmentResult extends PipelineResult implements HasAnatomicalArea, HasImageStack, IsAligned {

    private String legacyName;
	private String anatomicalArea;
    private String imageSize;
    private String opticalResolution;
    private String channelColors;
    private String channelSpec;
    private String objective;
    private String alignmentSpace;
    private String boundingBox;
    private Map<AlignmentScoreType, String> scores = new HashMap<>();
    private Long bridgeParentAlignmentId;

    public String getLegacyName() {
        return legacyName;
    }

    public void setLegacyName(String legacyName) {
        this.legacyName = legacyName;
    }

    @Override
    public String getAnatomicalArea() {
        return anatomicalArea;
    }

    public void setAnatomicalArea(String anatomicalArea) {
        this.anatomicalArea = anatomicalArea;
    }
    
    @Override
    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public String getOpticalResolution() {
        return opticalResolution;
    }

    public void setOpticalResolution(String opticalResolution) {
        this.opticalResolution = opticalResolution;
    }

    @Override
    public String getChannelColors() {
        return channelColors;
    }

    public void setChannelColors(String channelColors) {
        this.channelColors = channelColors;
    }

    @Override
    public String getChannelSpec() {
        return channelSpec;
    }

    public void setChannelSpec(String chanSpec) {
        this.channelSpec = chanSpec;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }
    
    public String getAlignmentSpace() {
        return alignmentSpace;
    }

    public void setAlignmentSpace(String alignmentSpace) {
        this.alignmentSpace = alignmentSpace;
    }

    public String getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(String boundingBox) {
        this.boundingBox = boundingBox;
    }

    public Map<AlignmentScoreType, String> getScores() {
        return scores;
    }

    public void setScores(Map<AlignmentScoreType, String> scores) {
        if (scores==null) throw new IllegalArgumentException("Property cannot be null");
        this.scores = scores;
    }

    public Long getBridgeParentAlignmentId() {
        return bridgeParentAlignmentId;
    }

    public void setBridgeParentAlignmentId(Long bridgeParentAlignmentId) {
        this.bridgeParentAlignmentId = bridgeParentAlignmentId;
    }
}
