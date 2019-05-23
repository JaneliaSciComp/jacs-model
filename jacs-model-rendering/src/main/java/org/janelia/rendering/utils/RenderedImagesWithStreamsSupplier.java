package org.janelia.rendering.utils;

import org.janelia.rendering.RenderedImagesWithStreams;

import java.util.Optional;

public interface RenderedImagesWithStreamsSupplier {
    Optional<RenderedImagesWithStreams> get();
}
