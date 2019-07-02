package org.janelia.filecacheutils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListeners;

public class LocalFileCache<K extends FileKey> {

    private final LoadingCache<K, FileProxy> localCache;

    public LocalFileCache(LocalFileCacheStorage localFileCacheStorage, RemoteFileRetriever<K> remoteFileRetriever, Executor asyncRemovalExecutor) {
        this.localCache =
                CacheBuilder.newBuilder()
                        .concurrencyLevel(1)
                        .maximumWeight(localFileCacheStorage.getCapacityInKB())
                        .weigher((FileKey key, FileProxy value) -> {
                            long sizeInKB = LocalFileCacheStorage.BYTES_TO_KB.apply(value.getSizeInBytes());
                            return sizeInKB > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sizeInKB;
                        })
                        .removalListener(RemovalListeners.asynchronous(new CachedFileRemovalListener(), asyncRemovalExecutor))
                        .build(new RemoteFileLoader<>(localFileCacheStorage, remoteFileRetriever));
    }

    public FileProxy getCachedFileEntry(K cachedFileKey, boolean forceRefresh) {
        if (forceRefresh) {
            localCache.invalidate(cachedFileKey);
        }
        try {
            return localCache.get(cachedFileKey);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }
}
