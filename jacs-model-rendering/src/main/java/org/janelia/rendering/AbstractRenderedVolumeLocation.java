package org.janelia.rendering;

import java.io.InputStream;

abstract class AbstractRenderedVolumeLocation implements RenderedVolumeLocation {

    static final String RAW_CH_TIFF_PATTERN = "-ngc.%s.tif";
    static final String TRANSFORM_FILE_NAME = "transform.txt";
    static final String TILED_VOL_BASE_FILE_NAME = "tilebase.cache.yml";

    void closeContentStream(InputStream contentStream) {
        if (contentStream != null) {
            try {
                contentStream.close();
            } catch (Exception ignore) {
            }
        }
    }

}
