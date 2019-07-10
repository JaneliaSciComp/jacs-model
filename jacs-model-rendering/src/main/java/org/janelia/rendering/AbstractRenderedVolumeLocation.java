package org.janelia.rendering;

abstract class AbstractRenderedVolumeLocation implements RenderedVolumeLocation {

    void closeContentStream(StreamableContent contentStream) {
        if (contentStream != null) {
            try {
                contentStream.getStream().close();
            } catch (Exception ignore) {
            }
        }
    }

}
