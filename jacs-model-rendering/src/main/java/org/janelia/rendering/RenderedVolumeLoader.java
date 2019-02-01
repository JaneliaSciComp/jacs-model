package org.janelia.rendering;

import java.util.Optional;

public interface RenderedVolumeLoader {
    Optional<RenderedVolume> loadVolume(RenderedVolumeLocation rvl);
    Optional<byte[]> loadSlice(RenderedVolume renderedVolume, TileKey tileKey);
    Optional<RawImage> findClosestRawImageFromVoxelCoord(RenderedVolumeLocation rvl, int xVoxel, int yVoxel, int zVoxel);
    byte[] loadRawImageContentFromVoxelCoord(RawImage rawImage,
                                             int xVoxel, int yVoxel, int zVoxel,
                                             int dimx, int dimy, int dimz, int channel);
}
