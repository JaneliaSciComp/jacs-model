package org.janelia.rendering;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

public interface RenderedVolumeLocation {
    /**
     * Rendered volume base URI.
     * Note: This does not uniquely identify the location of the data.
     * For uniquely identifying the data both baseURI and renderedVolumePath are required.
     * @return
     */
    URI getBaseURI();

    String getRenderedVolumePath();

    default URI getVolumeLocation() {
        String renderedVolumePath = getRenderedVolumePath();
        if (renderedVolumePath == null || renderedVolumePath.trim().length() == 0) {
            return getBaseURI();
        } else if (renderedVolumePath.trim().endsWith("/")) {
            return getBaseURI().resolve(renderedVolumePath.trim());
        } else {
            return getBaseURI().resolve(renderedVolumePath.trim() + "/");
        }
    }

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
     * Stream tile image content.
     * @param tileRelativePath
     * @return
     */
    InputStream streamTileImageContent(String tileRelativePath);

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
