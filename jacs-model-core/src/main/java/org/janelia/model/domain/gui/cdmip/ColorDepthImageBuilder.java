package org.janelia.model.domain.gui.cdmip;

import com.google.common.base.Preconditions;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.flyem.EMBody;
import org.janelia.model.domain.sample.Sample;

public class ColorDepthImageBuilder {

    private final ColorDepthImage colorDepthImage;

    public ColorDepthImageBuilder(ColorDepthImage colorDepthImage) {
        this.colorDepthImage = colorDepthImage;
    }

    public ColorDepthImageBuilder withLMSample(Sample sample) {
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

    public ColorDepthImageBuilder withEMBody(EMBody emBody) {
        colorDepthImage.setEmBody(emBody);
        if (emBody != null) {
            if (colorDepthImage.getEmBodyRef() == null) {
                colorDepthImage.setEmBodyRef(Reference.createFor(emBody));
            } else {
                Preconditions.checkState(colorDepthImage.getEmBodyRef().equals(Reference.createFor(emBody)));
            }
        } else {
            colorDepthImage.setEmBodyRef(null);
        }
        return this;
    }

    public ColorDepthImage build() {
        return colorDepthImage;
    }
}
