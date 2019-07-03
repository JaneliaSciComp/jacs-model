package org.janelia.filecacheutils;

import java.nio.file.Path;

public interface FileKey {
    Path getLocalPath(LocalFileCacheStorage localFileCacheStorage);
    String getRemoteFileName();
}
