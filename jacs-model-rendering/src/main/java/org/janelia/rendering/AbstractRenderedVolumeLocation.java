package org.janelia.rendering;

import java.io.InputStream;

abstract class AbstractRenderedVolumeLocation implements RenderedVolumeLocation {

    void closeContentStream(InputStream contentStream) {
        if (contentStream != null) {
            try {
                contentStream.close();
            } catch (Exception ignore) {
            }
        }
    }

}
