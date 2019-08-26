package org.janelia.filecacheutils;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListeners;

/**
 * LocalFileCache implements the file cache.
 *
 * @param <K> file cache key
 */
public class LocalFileCache<K extends FileKey> {
    /**
     * In memory cache - the value is an optional so that it won't perform extra searches for keys that actually don't exist, such as missing files.
     */
    private final LoadingCache<K, Optional<FileProxy>> localCache;

    /**
     * LocalFileCache constructor.
     *
     * @param localFileCacheStorage local file cache
     * @param cacheConcurrency cache concurrency parameter - if the value is <= 0 it is ignored
     * @param keyToProxySupplier is used by the remotecache loader for loading the file proxy from the corresponding cache entry key
     * @param asyncRemovalExecutor thread executor that runs the cache eviction process
     * @param localFileWriterExecutor thread executor in which the local file writer should run
     */
    public LocalFileCache(LocalFileCacheStorage localFileCacheStorage,
                          int cacheConcurrency,
                          FileKeyToProxySupplier<K> keyToProxySupplier,
                          Executor asyncRemovalExecutor,
                          ExecutorService localFileWriterExecutor) {
        CacheBuilder<K, Optional<FileProxy>> cacheBuilder = CacheBuilder.newBuilder()
                .maximumWeight(localFileCacheStorage.getCapacityInKB()) // the in memory cache uses the cache entry weigh and maximum weight for the eviction policy
                .weigher((FileKey key, Optional<FileProxy> value) -> {
                    long sizeInKB = LocalFileCacheStorage.BYTES_TO_KB.apply(value.flatMap(fp -> fp.estimateSizeInBytes()).orElse(1L));
                    return sizeInKB > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sizeInKB;
                })
                .removalListener(RemovalListeners.asynchronous(new CachedFileRemovalListener(), asyncRemovalExecutor));
        if (cacheConcurrency > 0) {
            cacheBuilder.concurrencyLevel(cacheConcurrency);
        }
        this.localCache = cacheBuilder.build(new RemoteFileLoader<>(localFileCacheStorage, keyToProxySupplier, localFileWriterExecutor));

    }

    @Nullable
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
