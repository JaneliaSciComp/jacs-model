package org.janelia.filecacheutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Proxy for accessing content.
 */
public interface FileProxy {
    String getFileId();

    Long estimateSizeInBytes();

    InputStream openContentStream() throws FileNotFoundException;

    File getLocalFile() throws FileNotFoundException;

    boolean exists();

    boolean deleteProxy();
}
