package org.janelia.rendering;

import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;

public interface RenderedVolumeLocation extends DataLocation {
    /**
     * List image URIs at the specified detail level.
     */
    List<URI> listImageUris(int level);

    /**
     * Read tile image info.
     * @param tileRelativePath
     * @return
     */
    @Nullable RenderedImageInfo readTileImageInfo(String tileRelativePath, StorageOptions storageOptions);

    /**
     * Read tile image as texture bytes.
     */
    Streamable<byte[]> readTiffPageAsTexturedBytes(String imageRelativePath, List<String> channelImageNames, int pageNumber, StorageOptions storageOptions);

    /**
     * Read ROI from the raw image.
     * @return
     */
    Streamable<byte[]> readTiffImageROIPixels(String imagePath, int xCenter, int yCenter, int zCenter, int dimx, int dimy, int dimz, StorageOptions storageOptions);

}
