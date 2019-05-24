package org.janelia.rendering;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;

public class RenderedImageWithStream {
    private final RenderedImage renderedImage;
    private final InputStream renderedImageStream;

    RenderedImageWithStream(RenderedImage renderedImage, InputStream renderedImageStream) {
        this.renderedImage = renderedImage;
        this.renderedImageStream = renderedImageStream;
    }

    public RenderedImage getRenderedImage() {
        return renderedImage;
    }

    public void close() {
        if (renderedImageStream != null) {
            try {
                renderedImageStream.close();
            } catch (IOException ignore) {
            }
        }
    }

}
