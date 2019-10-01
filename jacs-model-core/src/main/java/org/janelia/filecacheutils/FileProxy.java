package org.janelia.filecacheutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

/**
 * Proxy for accessing content.
 */
public interface FileProxy {
    String getFileId();

    Long estimateSizeInBytes();

    InputStream openContentStream() throws FileNotFoundException;

    File getLocalFile() throws FileNotFoundException;

    boolean deleteProxy();
}
