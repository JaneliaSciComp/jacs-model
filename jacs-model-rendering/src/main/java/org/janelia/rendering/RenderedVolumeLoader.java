package org.janelia.rendering;

import java.util.List;
import java.util.Optional;

import org.janelia.rendering.ymlrepr.RawVolData;

public interface RenderedVolumeLoader {
    /**
     * Default transform file name.
     */
    String DEFAULT_TRANSFORM_FILE_NAME = "transform.txt";
    /**
     * Default tilebase file name.
     */
    String DEFAULT_TILED_VOL_BASE_FILE_NAME = "tilebase.cache.yml";

    Optional<RenderedVolumeMetadata> loadVolume(RenderedVolumeLocation rvl, StorageOptions storageOptions);

    Streamable<byte[]> loadSlice(RenderedVolumeLocation rvl, RenderedVolumeMetadata renderedVolumeMetadata, TileKey tileKey, StorageOptions storageOptions);

    Optional<RawImage> findClosestRawImageFromVoxelCoord(RenderedVolumeLocation rvl, int xVoxel, int yVoxel, int zVoxel, StorageOptions storageOptions);

    Streamable<byte[]> loadRawImageContentFromVoxelCoord(RenderedVolumeLocation rvl,
                                                         RawImage rawImage,
                                                         int channel,
                                                         int xVoxel, int yVoxel, int zVoxel,
                                                         int dimx, int dimy, int dimz,
                                                         StorageOptions storageOptions);

    List<RawImage> loadVolumeRawImageTiles(RenderedVolumeLocation rvl, StorageOptions storageOptions);

    RawVolData loadRawVolumeData(RenderedVolumeLocation rvl, StorageOptions storageOptions);
}
