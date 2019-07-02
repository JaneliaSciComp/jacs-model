package org.janelia.filecacheutils;

import java.nio.file.Path;

import com.google.common.cache.CacheLoader;

public class RemoteFileLoader<K extends FileKey> extends CacheLoader<K, FileProxy> {

    private final LocalFileCacheStorage localFileCacheStorage;
    private final RemoteFileRetriever<K> fileRetriever;

    RemoteFileLoader(LocalFileCacheStorage localFileCacheStorage, RemoteFileRetriever<K> fileRetriever) {
        this.localFileCacheStorage = localFileCacheStorage;
        this.fileRetriever = fileRetriever;
    }

    @Override
    public FileProxy load(K cachedFileKey) {
        Path localPath = cachedFileKey.getLocalPath(localFileCacheStorage);
        if (localPath == null) {
            throw new IllegalArgumentException("Local path cannot be retrieved from cachedFileKey " + cachedFileKey);
        }
        Path tempLocalPath = cachedFileKey.getTempLocalPath(localFileCacheStorage);
        return new CachedFileProxy(localPath, tempLocalPath, fileRetriever.retrieve(cachedFileKey), localFileCacheStorage);
    }
}
