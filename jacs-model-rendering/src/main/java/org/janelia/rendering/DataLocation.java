package org.janelia.rendering;

import java.io.InputStream;
import java.net.URI;

public interface DataLocation {
    /**
     * @return the base connection URI for retrieving rendered volume data.
     */
    URI getConnectionURI();

    /**
     * The base URI of the storage that holds the rendered volume.
     * Note: This does not uniquely identify the location of the data.
     * For uniquely identifying the data both baseURI and renderedVolumePath are required.
     * @return
     */
    URI getDataStorageURI();

    String getBaseDataStoragePath();

    default URI getBaseStorageLocationURI() {
        String baseStoragePath = getBaseDataStoragePath();
        if (baseStoragePath == null || baseStoragePath.trim().length() == 0) {
            return getDataStorageURI();
        } else if (baseStoragePath.trim().endsWith("/")) {
            return getDataStorageURI().resolve(baseStoragePath.trim());
        } else {
            return getDataStorageURI().resolve(baseStoragePath.trim() + "/");
        }
    }

    /**
     * Stream content from relative path.
     * @param relativePath
     * @return
     */
    Streamable<InputStream> getContentFromRelativePath(String relativePath);

    /**
     * Stream content from absolute path.
     * @param absolutePath
     * @return
     */
    Streamable<InputStream> getContentFromAbsolutePath(String absolutePath);

    /**
     * Check if content exists at relative path.
     * @param relativePath
     * @return
     */
    boolean checkContentAtRelativePath(String relativePath);

    /**
     * Check if content exists at absolute path.
     * @param absolutePath
     * @return
     */
    boolean checkContentAtAbsolutePath(String absolutePath);
}
