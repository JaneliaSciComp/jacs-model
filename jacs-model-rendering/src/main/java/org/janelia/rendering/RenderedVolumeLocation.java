package org.janelia.rendering;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

public interface RenderedVolumeLocation {
    /**
     * @return the base connection URI for retrieving rendered volume data.
     */
    URI getConnectionURI();

    /**
     * The base URI of the storage that holds the rendered volume.
     * Note: This does not uniquely identify the location of the data.
     * For uniquely identifying the data both baseURI and renderedVolumePath are required.
     * @return
     */
    URI getDataStorageURI();

    String getRenderedVolumePath();

    default URI getVolumeLocation() {
        String renderedVolumePath = getRenderedVolumePath();
        if (renderedVolumePath == null || renderedVolumePath.trim().length() == 0) {
            return getDataStorageURI();
        } else if (renderedVolumePath.trim().endsWith("/")) {
            return getDataStorageURI().resolve(renderedVolumePath.trim());
        } else {
            return getDataStorageURI().resolve(renderedVolumePath.trim() + "/");
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
     * Read tile image as texture bytes.
     */
    @Nullable byte[] readTileImagePageAsTexturedBytes(String tileRelativePath, List<String> channelImageNames, int pageNumber);

    /**
     * Read ROI from the raw image.
     * @return
     */
    @Nullable byte[] readRawTileROIPixels(RawImage rawImage, int channel, int xCenter, int yCenter, int zCenter, int dimx, int dimy, int dimz);

    /**
     * Read transform.txt
\     */
    @Nullable InputStream readTransformData();

    /**
     * Read tilebase.cache.yml
     */
    @Nullable InputStream readTileBaseData();

    /**
     * Stream entire raw image.
     * @return
     */
    @Nullable InputStream readRawTileContent(RawImage rawImage, int channel);

    /**
     * Stream content from relative path.
     * @param relativePath
     * @return
     */
    @Nullable InputStream streamContentFromRelativePath(String relativePath);

    /**
     * Stream content from absolute path.
     * @param absolutePath
     * @return
     */
    @Nullable InputStream streamContentFromAbsolutePath(String absolutePath);
}
