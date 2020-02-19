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

    /**
     * Return the local file copy. This method may first copy the remote file to the local storage and then return it
     * if the remote file is not mountable on a locally accessible volume.
     *
     * @return
     * @throws FileNotFoundException
     */
    File getLocalFile() throws FileNotFoundException;

    boolean exists();

    boolean deleteProxy();
}
