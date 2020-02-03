package org.janelia.model.domain.gui.cdmip;

import com.google.common.base.Preconditions;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.sample.Sample;

public class ColorDepthImageWithSampleBuilder {

    private final ColorDepthImage colorDepthImage;

    public ColorDepthImageWithSampleBuilder(ColorDepthImage colorDepthImage) {
        this.colorDepthImage = colorDepthImage;
    }

    public ColorDepthImageWithSampleBuilder withSample(Sample sample) {
        colorDepthImage.setSample(sample);
        if (sample != null) {
            if (colorDepthImage.getSampleRef() == null) {
                colorDepthImage.setSampleRef(Reference.createFor(sample));
            } else {
                Preconditions.checkState(colorDepthImage.getSampleRef().equals(Reference.createFor(sample)));
            }
        } else {
            colorDepthImage.setSampleRef(null);
        }
        return this;
    }

    public ColorDepthImage build() {
        return colorDepthImage;
    }
}
