package org.janelia.model.domain.sample;

import org.janelia.model.domain.interfaces.HasAnatomicalArea;
import org.janelia.model.domain.interfaces.HasImageStack;

/**
 * The result of processing the LSMs of a single anatomical area
 * of an ObjectiveSample. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleProcessingResult extends PipelineResult implements HasAnatomicalArea, HasImageStack {
	
	private String anatomicalArea;
    private String imageSize;
    private String opticalResolution;
    private String channelColors;
    private String chanelSpec;
    private boolean distortionCorrected;

    public String getAnatomicalArea() {
        return anatomicalArea;
    }

    public void setAnatomicalArea(String anatomicalArea) {
        this.anatomicalArea = anatomicalArea;
    }
    
    public String getImageSize() {
        return imageSize;
    }
    
    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }
    
    public String getOpticalResolution() {
        return opticalResolution;
    }
    
    public void setOpticalResolution(String opticalResolution) {
        this.opticalResolution = opticalResolution;
    }
    
    public String getChannelColors() {
        return channelColors;
    }
    
    public void setChannelColors(String channelColors) {
        this.channelColors = channelColors;
    }
    
    public String getChannelSpec() {
        return chanelSpec;
    }
    
    public void setChannelSpec(String chanSpec) {
        this.chanelSpec = chanSpec;
    }

    public boolean isDistortionCorrected() {
        return distortionCorrected;
    }

    public void setDistortionCorrected(boolean distortionCorrected) {
        this.distortionCorrected = distortionCorrected;
    }
}
