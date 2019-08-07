package org.janelia.filecacheutils;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Proxy for accessing content.
 */
public interface FileProxy {
    String getFileId();

    Optional<Long> estimateSizeInBytes();

    @Nullable InputStream openContentStream();

    File getLocalFile();

    boolean deleteProxy();
}
