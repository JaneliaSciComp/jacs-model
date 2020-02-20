package org.janelia.filecacheutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Proxy for accessing content.
 */
public interface FileProxy {
    String getFileId();

    /**
     * Estimate the file size.
     *
     * @param alwaysCheck always try to get the latest value
     * @return the estimated size or null or 0L if the size cannot be estimated
     */
    Long estimateSizeInBytes(boolean alwaysCheck);

    /**
     * Return a stream to this file proxy.
     *
     * @param alwaysDownload if true it always downloads the latest version
     * @return
     * @throws FileNotFoundException
     */
    InputStream openContentStream(boolean alwaysDownload) throws FileNotFoundException;

    /**
     * Return the local file copy. This method may first copy the remote file to the local storage and then return it
     * if the remote file is not mountable on a locally accessible volume.
     *
     * @param alwaysDownload if true it always downloads the latest version
     * @return
     * @throws FileNotFoundException
     */
    File getLocalFile(boolean alwaysDownload) throws FileNotFoundException;

    /**
     *
     * @param alwaysCheck if true always check
     * @return
     */
    boolean exists(boolean alwaysCheck);

    boolean deleteProxy();
}
