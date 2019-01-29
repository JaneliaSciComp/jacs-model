package org.janelia.rendering;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface RenderedVolumeLoader {
    Optional<RenderedVolume> loadVolume(Path basePath);
    Optional<byte[]> loadSlice(RenderedVolume renderedVolume, TileKey tileKey);
    Optional<RawImage> findClosestRawImageFromVoxelCoord(Path basePath, int xVoxel, int yVoxel, int zVoxel);
    byte[] loadRawImageContentFromVoxelCoord(RawImage rawImage,
                                             int xVoxel, int yVoxel, int zVoxel,
                                             int dimx, int dimy, int dimz, int channel);
}
