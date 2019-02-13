package org.janelia.rendering;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RenderedVolumeLocation {
    URI getBaseURI();

    /**
     * List image URIs at the specified detail level.
     */
    List<URI> listImageUris(int level);

    /**
     * Read tile image info.
     * @param tileRelativePath
     * @return
     */
    RenderedImageInfo readTileImageInfo(String tileRelativePath);

    /**
     * Read tile image as texture bytes.
     */
    @Nullable byte[] readTileImagePageAsTexturedBytes(String tileRelativePath, List<String> channelImageNames, int pageNumber);

    /**
     * Read ROI from the raw image.
     * @return
     */
    byte[] readRawTileROIPixels(RawImage rawImage, int channel, int xCenter, int yCenter, int zCenter, int dimx, int dimy, int dimz);

    /**
     * Read transform.txt
\     */
    @Nullable InputStream readTransformData();

    /**
     * Read tilebase.cache.yml
     */
    @Nullable InputStream readTileBaseData();
}
