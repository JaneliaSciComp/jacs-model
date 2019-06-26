package org.janelia.cacheutils;

import java.nio.file.Path;
import java.util.Map;

public interface CachedFileKey {
    Path getLocalPath(LocalFileCacheStorage localFileCacheStorage);
    Path getTempLocalPath(LocalFileCacheStorage localFileCacheStorage);
    String getRemoteFileName();
    Map<String, String> getRemoteFileParams();
}
