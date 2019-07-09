package org.janelia.filecacheutils;

import java.nio.file.Path;
import java.util.Optional;

import com.google.common.cache.CacheLoader;

public class RemoteFileLoader<K extends FileKey> extends CacheLoader<K, Optional<FileProxy>> {

    private final LocalFileCacheStorage localFileCacheStorage;
    private final FileKeyToProxySupplier<K> fileKeyToProxySupplier;

    RemoteFileLoader(LocalFileCacheStorage localFileCacheStorage, FileKeyToProxySupplier<K> fileKeyToProxySupplier) {
        this.localFileCacheStorage = localFileCacheStorage;
        this.fileKeyToProxySupplier = fileKeyToProxySupplier;
    }

    @Override
    public Optional<FileProxy> load(K cachedFileKey) {
        Path localPath = cachedFileKey.getLocalPath(localFileCacheStorage);
        if (localPath == null) {
            throw new IllegalArgumentException("Local path cannot be retrieved from cachedFileKey " + cachedFileKey);
        }
        return Optional.of(new CachedFileProxy(localPath, fileKeyToProxySupplier.getProxyFromKey(cachedFileKey), localFileCacheStorage));
    }
}
