package org.janelia.filecacheutils;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import com.google.common.cache.CacheLoader;

/**
 * Local file cache loader.
 *
 * @param <K> cache entry key
 */
public class RemoteFileLoader<K extends FileKey> extends CacheLoader<K, Optional<FileProxy>> {

    private final LocalFileCacheStorage localFileCacheStorage;
    private final FileKeyToProxySupplier<K> fileKeyToProxySupplier;
    private final ExecutorService localFileWriterExecutor;

    RemoteFileLoader(LocalFileCacheStorage localFileCacheStorage, FileKeyToProxySupplier<K> fileKeyToProxySupplier, ExecutorService localFileWriterExecutor) {
        this.localFileCacheStorage = localFileCacheStorage;
        this.fileKeyToProxySupplier = fileKeyToProxySupplier;
        this.localFileWriterExecutor = localFileWriterExecutor;
    }

    @Override
    public Optional<FileProxy> load(K cachedFileKey) {
        Path localPath = cachedFileKey.getLocalPath(localFileCacheStorage);
        if (localPath == null) {
            throw new IllegalArgumentException("Local path cannot be retrieved from cachedFileKey " + cachedFileKey);
        }
        return Optional.of(new CachedFileProxy(localPath, fileKeyToProxySupplier.getProxyFromKey(cachedFileKey), localFileCacheStorage, localFileWriterExecutor));
    }
}
