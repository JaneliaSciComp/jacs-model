package org.janelia.rendering;

import java.util.Optional;
import java.util.stream.Stream;

public interface RenderedVolumeLoader {
    Optional<RenderedVolume> loadVolume(RenderedVolumeLocation rvl);
    Optional<byte[]> loadSlice(RenderedVolume renderedVolume, TileKey tileKey);
    Optional<RawImage> findClosestRawImageFromVoxelCoord(RenderedVolumeLocation rvl, int xVoxel, int yVoxel, int zVoxel);
    byte[] loadRawImageContentFromVoxelCoord(RenderedVolumeLocation rvl,
                                             RawImage rawImage,
                                             int channel,
                                             int xVoxel, int yVoxel, int zVoxel,
                                             int dimx, int dimy, int dimz);
    Stream<RawImage> streamVolumeRawImageTiles(RenderedVolumeLocation rvl);
}
