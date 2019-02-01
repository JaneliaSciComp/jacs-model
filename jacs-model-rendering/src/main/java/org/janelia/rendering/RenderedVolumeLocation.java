package org.janelia.rendering;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface RenderedVolumeLocation {
    String TRANSFORM_FILE_NAME = "transform.txt";
    String TILED_VOL_BASE_FILE_NAME = "tilebase.cache.yml";

    URI getBaseURI();

    /**
     * List image URIs at the specified detail level.
     */
    List<URI> listImageUris(int level);

    /**
     * Read tile image
\    */
    @Nullable InputStream readTileImage(String tileRelativePath);

    /**
     * Read tile image
     */
    @Nullable byte[] readTileImagePages(String tileRelativePath, int startPage, int nPages);

    /**
     * Read transform.txt
\     */
    @Nullable InputStream readTransformData();

    /**
     * Read tilebase.cache.yml
     */
    @Nullable InputStream readTileBaseData();
}
