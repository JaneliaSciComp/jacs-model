package org.janelia.rendering;

import java.io.InputStream;

abstract class AbstractRenderedVolumeLocation implements RenderedVolumeLocation {

    static final String RAW_CH_TIFF_PATTERN = "-ngc.%s.tif";

    void closeContentStream(InputStream contentStream) {
        if (contentStream != null) {
            try {
                contentStream.close();
            } catch (Exception ignore) {
            }
        }
    }

}
