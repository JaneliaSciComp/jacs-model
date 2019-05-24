package org.janelia.rendering;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RenderedImagesWithStreams {
    private final List<RenderedImage> renderedImages = new ArrayList<>();
    private final List<InputStream> renderedImageStreams = new ArrayList<>();

    public static RenderedImagesWithStreams empty() {
        return new RenderedImagesWithStreams();
    }

    public static RenderedImagesWithStreams withImageOnly(RenderedImage renderedImage) {
        RenderedImagesWithStreams rims = new RenderedImagesWithStreams();
        rims.addImageAndStream(renderedImage, null);
        return rims;
    }

    public static RenderedImagesWithStreams withImageAndStream(RenderedImage renderedImage, InputStream rennderedImageStream) {
        RenderedImagesWithStreams rims = new RenderedImagesWithStreams();
        rims.addImageAndStream(renderedImage, rennderedImageStream);
        return rims;
    }

    private RenderedImagesWithStreams() {
    }

    public RenderedImagesWithStreams append(RenderedImagesWithStreams rims) {
        this.renderedImages.addAll(rims.renderedImages);
        this.renderedImageStreams.addAll(rims.renderedImageStreams);
        return this;
    }

    public void close() {
        renderedImageStreams.stream()
                .filter(is -> is != null)
                .forEach(is -> {
                    try {
                        is.close();
                    } catch (IOException ignore) {
                    }
                });
    }

    public RenderedImagesWithStreams combine(String operation) {
        if (renderedImages.size() == 0) {
            return null;
        } else if (renderedImages.size() == 1) {
            return this
        } else {
            ParameterBlock combinedImages = new ParameterBlockJAI(operation);
            renderedImages.forEach(rim -> combinedImages.addSource(rim));
            return JAI.create(operation, combinedImages, null);
        }
    }

    private void addImageAndStream(RenderedImage renderedImage, InputStream renderedImageStream) {
        if (renderedImage != null) {
            renderedImages.add(renderedImage);
            renderedImageStreams.add(renderedImageStream);
        }
    }

}
