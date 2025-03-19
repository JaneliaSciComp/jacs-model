package org.janelia.rendering;

import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenderedImagesWithStreams {
    private static final Logger LOG = LoggerFactory.getLogger(RenderedImagesWithStreams.class);

    private final List<String> renderedImageNames = new ArrayList<>();
    private final List<RenderedImage> renderedImages = new ArrayList<>();
    private final List<InputStream> renderedImageStreams = new ArrayList<>();

    public static RenderedImagesWithStreams empty() {
        return new RenderedImagesWithStreams();
    }

    public static RenderedImagesWithStreams withImageAndStream(String renderedImageName, RenderedImage renderedImage, InputStream rennderedImageStream) {
        RenderedImagesWithStreams rims = new RenderedImagesWithStreams();
        rims.addImageAndStream(renderedImageName, renderedImage, rennderedImageStream);
        return rims;
    }

    private RenderedImagesWithStreams() {
    }

    public RenderedImagesWithStreams append(RenderedImagesWithStreams rims) {
        this.renderedImageNames.addAll(rims.renderedImageNames);
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

    public RenderedImageWithStream combine(String operation) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (renderedImages.isEmpty()) {
                return null;
            } else if (renderedImages.size() == 1) {
                return new RenderedImageWithStream(renderedImages.get(0), renderedImageStreams.get(0));
            } else {
                ParameterBlock combinedImages = new ParameterBlockJAI(operation);
                renderedImages.forEach(combinedImages::addSource);
                LOG.debug("Adding all sources {} for {} took {} ms", renderedImageNames, operation, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new RenderedImageWithStream(
                        JAI.create(operation, combinedImages, null),
                        new InputStream() {
                            @Override
                            public int read() {
                                throw new UnsupportedOperationException("Read is not supported from a final combined stream which is created only for being able to close all underlying streams");
                            }

                            @Override
                            public void close() {
                                RenderedImagesWithStreams.this.close();
                            }
                        }
                );
            }
        } finally {
            LOG.debug("Combining {} with {} took {} ms", renderedImageNames, operation, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    public List<String> getRenderedImageNames() {
        return renderedImageNames;
    }

    private void addImageAndStream(String renderedImageName, RenderedImage renderedImage, InputStream renderedImageStream) {
        if (renderedImage != null) {
            renderedImageNames.add(renderedImageName);
            renderedImages.add(renderedImage);
            renderedImageStreams.add(renderedImageStream);
        }
    }

}
