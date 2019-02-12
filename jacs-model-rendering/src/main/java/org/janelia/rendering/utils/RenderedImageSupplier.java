package org.janelia.rendering.utils;

import java.awt.image.RenderedImage;
import java.util.Optional;

public interface RenderedImageSupplier {
    Optional<RenderedImage> get();
}
