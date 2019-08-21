package org.janelia.rendering;

import java.util.List;
import java.util.Optional;

import org.janelia.rendering.ymlrepr.RawVolData;

public interface RenderedVolumeLoader {
    Optional<RenderedVolumeMetadata> loadVolume(RenderedVolumeLocation rvl);

    Optional<StreamableContent> loadSlice(RenderedVolumeLocation rvl, RenderedVolumeMetadata renderedVolumeMetadata, TileKey tileKey);

    Optional<RawImage> findClosestRawImageFromVoxelCoord(RenderedVolumeLocation rvl, int xVoxel, int yVoxel, int zVoxel);

    Optional<StreamableContent> loadRawImageContentFromVoxelCoord(RenderedVolumeLocation rvl,
                                                                  RawImage rawImage,
                                                                  int channel,
                                                                  int xVoxel, int yVoxel, int zVoxel,
                                                                  int dimx, int dimy, int dimz);

    List<RawImage> loadVolumeRawImageTiles(RenderedVolumeLocation rvl);

    RawVolData loadRawVolumeData(RenderedVolumeLocation rvl);
}
