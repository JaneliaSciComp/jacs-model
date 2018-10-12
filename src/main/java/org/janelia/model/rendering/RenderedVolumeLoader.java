package org.janelia.model.rendering;

import java.nio.file.Path;
import java.util.Optional;

public interface RenderedVolumeLoader {
    Optional<RenderedVolume> loadVolume(Path basePath);
    Optional<byte[]> loadSlice(RenderedVolume renderedVolume, TileIndex tileIndex);
}
