package org.janelia.model.domain.gui.cdmip;

import com.google.common.base.Preconditions;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.flyem.EMBody;
import org.janelia.model.domain.sample.Sample;

public class ColorDepthImageWithNeuronsBuilder {

    private final ColorDepthImage colorDepthImage;

    public ColorDepthImageWithNeuronsBuilder(ColorDepthImage colorDepthImage) {
        this.colorDepthImage = colorDepthImage;
    }

    public ColorDepthImageWithNeuronsBuilder withLMSample(Sample sample) {
        colorDepthImage.setSample(sample);
        if (sample != null) {
            if (colorDepthImage.getSampleRef() == null) {
                colorDepthImage.setSampleRef(Reference.createFor(sample));
            } else {
                Preconditions.checkState(colorDepthImage.getSampleRef().equals(Reference.createFor(sample)));
            }
        }
        return this;
    }

    public ColorDepthImageWithNeuronsBuilder withEMBody(EMBody emBody) {
        colorDepthImage.setEmBody(emBody);
        if (emBody != null) {
            if (colorDepthImage.getEmBodyRef() == null) {
                colorDepthImage.setEmBodyRef(Reference.createFor(emBody));
            } else {
                Preconditions.checkState(colorDepthImage.getEmBodyRef().equals(Reference.createFor(emBody)));
            }
        }
        return this;
    }

    public ColorDepthImage build() {
        return colorDepthImage;
    }
}
