package org.janelia.rendering;

import java.util.List;
import java.util.Optional;

public interface RenderedVolumeLoader {
    Optional<RenderedVolumeMetadata> loadVolume(RenderedVolumeLocation rvl);

    Optional<byte[]> loadSlice(RenderedVolumeLocation rvl, RenderedVolumeMetadata renderedVolumeMetadata, TileKey tileKey);

    Optional<RawImage> findClosestRawImageFromVoxelCoord(RenderedVolumeLocation rvl, int xVoxel, int yVoxel, int zVoxel);

    Optional<byte[]> loadRawImageContentFromVoxelCoord(RenderedVolumeLocation rvl,
                                                       RawImage rawImage,
                                                       int channel,
                                                       int xVoxel, int yVoxel, int zVoxel,
                                                       int dimx, int dimy, int dimz);

    List<RawImage> loadVolumeRawImageTiles(RenderedVolumeLocation rvl);
}
