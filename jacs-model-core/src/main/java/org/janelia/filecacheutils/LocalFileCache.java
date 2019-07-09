package org.janelia.filecacheutils;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListeners;

public class LocalFileCache<K extends FileKey> {

    private final LoadingCache<K, Optional<FileProxy>> localCache;

    public LocalFileCache(LocalFileCacheStorage localFileCacheStorage, FileKeyToProxySupplier<K> keyToProxySupplier, Executor asyncRemovalExecutor) {
        this.localCache =
                CacheBuilder.newBuilder()
                        .concurrencyLevel(2)
                        .maximumWeight(localFileCacheStorage.getCapacityInKB())
                        .weigher((FileKey key, Optional<FileProxy> value) -> {
                            long sizeInKB = LocalFileCacheStorage.BYTES_TO_KB.apply(value.map(fp -> fp.getSizeInBytes()).orElse(0L));
                            return sizeInKB > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sizeInKB;
                        })
                        .removalListener(RemovalListeners.asynchronous(new CachedFileRemovalListener(), asyncRemovalExecutor))
                        .build(new RemoteFileLoader<>(localFileCacheStorage, keyToProxySupplier));
    }

    public FileProxy getCachedFileEntry(K cachedFileKey, boolean forceRefresh) {
        if (forceRefresh) {
            localCache.invalidate(cachedFileKey);
        }
        try {
            return localCache.get(cachedFileKey).orElse(null);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }
}
