package org.janelia.rendering.utils;

import java.util.Optional;

import org.janelia.rendering.RenderedImagesWithStreams;

interface RenderedImagesWithStreamsSupplier {
    Optional<RenderedImagesWithStreams> get();
}
