package org.janelia.model.rendering;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface RenderedVolumeLoader {
    Optional<RenderedVolume> loadVolume(Path basePath);
    Optional<byte[]> loadSlice(RenderedVolume renderedVolume, TileKey tileKey);
    Optional<RawImage> findClosestRawImage(Path basePath, Integer x, Integer y, Integer z);
    byte[] loadRawImageContent(RawImage rawImage,
                               int x, int y, int z,
                               int dimx, int dimy, int dimz, int channel);
}
