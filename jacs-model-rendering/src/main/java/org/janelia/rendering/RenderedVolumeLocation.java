package org.janelia.rendering;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public interface RenderedVolumeLocation {
    /**
     * Default raw tile channel suffix pattern
     */
    String DEFAULT_RAW_CH_SUFFIX_PATTERN = "-ngc.%s.tif";
    /**
     * Default transform file name.
     */
    String DEFAULT_TRANSFORM_FILE_NAME = "transform.txt";
    /**
     * Default tilebase file name.
     */
    String DEFAULT_TILED_VOL_BASE_FILE_NAME = "tilebase.cache.yml";

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
    @Nullable RenderedImageInfo readTileImageInfo(String tileRelativePath);

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
    default Optional<StreamableContent> getTransformData() {
        return getContentFromRelativePath(DEFAULT_TRANSFORM_FILE_NAME);
    }

    /**
     * Read tilebase.cache.yml
     */
    default Optional<StreamableContent> getTileBaseData() {
        return getContentFromRelativePath(DEFAULT_TILED_VOL_BASE_FILE_NAME);
    }

    /**
     * Stream entire raw image.
     * @param rawImage
     * @param channel
     * @return
     */
    default Optional<StreamableContent> getRawTileContent(RawImage rawImage, int channel) {
        String rawImagePath = rawImage.getRawImagePath(String.format(DEFAULT_RAW_CH_SUFFIX_PATTERN, channel));
        return getContentFromAbsolutePath(rawImagePath);
    }

    /**
     * Stream content from relative path.
     * @param relativePath
     * @return
     */
    Optional<StreamableContent> getContentFromRelativePath(String relativePath);

    /**
     * Stream content from absolute path.
     * @param absolutePath
     * @return
     */
    Optional<StreamableContent> getContentFromAbsolutePath(String absolutePath);
}
