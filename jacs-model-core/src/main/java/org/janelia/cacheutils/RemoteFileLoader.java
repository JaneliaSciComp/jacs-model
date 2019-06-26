package org.janelia.cacheutils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import com.google.common.cache.CacheLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteFileLoader extends CacheLoader<CachedFileKey, CachedFile> {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteFileLoader.class);

    private final LocalFileCacheStorage localFileCacheStorage;
    private final RemoteFileRetriever fileRetriever;

    public RemoteFileLoader(LocalFileCacheStorage localFileCacheStorage, RemoteFileRetriever fileRetriever) {
        this.localFileCacheStorage = localFileCacheStorage;
        this.fileRetriever = fileRetriever;
    }

    @Override
    public CachedFile load(CachedFileKey cachedFileKey) {
        Path localPath = cachedFileKey.getLocalPath(localFileCacheStorage);
        if (localPath == null) {
            throw new IllegalArgumentException("Local path cannot be retrieved from cachedFileKey " + cachedFileKey);
        }
        Path tempLocalPath = cachedFileKey.getTempLocalPath(localFileCacheStorage);
        if (Files.exists(localPath)) {
            return new CachedFile(localPath, tempLocalPath, localFileCacheStorage);
        } else {
            return new CachedFile(localPath, tempLocalPath, localFileCacheStorage, fileRetriever.retrieveRemoteFile(cachedFileKey.getRemoteFileName(), cachedFileKey.getRemoteFileParams()));
        }
    }
}
