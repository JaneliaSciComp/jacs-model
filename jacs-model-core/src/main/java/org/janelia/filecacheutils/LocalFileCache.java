package org.janelia.filecacheutils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListeners;

import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * LocalFileCache implements the file cache.
 *
 * @param <K> file cache key
 */
public class LocalFileCache<K extends FileKey> {
    /**
     * In memory cache - the value is an optional so that it won't perform extra searches for keys that actually don't exist, such as missing files.
     */
    private final LoadingCache<K, FileProxy> localCache;

    /**
     * LocalFileCache constructor.
     *
     * @param localFileCacheStorage   local file cache
     * @param cacheConcurrency        cache concurrency parameter - if the value is <= 0 it is ignored
     * @param keyToProxySupplier      is used by the remotecache loader for loading the file proxy from the corresponding cache entry key
     * @param asyncRemovalExecutor    thread executor that runs the cache eviction process
     * @param localFileWriterExecutor thread executor in which the local file writer should run
     */
    public LocalFileCache(LocalFileCacheStorage localFileCacheStorage,
                          int cacheConcurrency,
                          FileKeyToProxyMapper<K> keyToProxySupplier,
                          Executor asyncRemovalExecutor,
                          ExecutorService localFileWriterExecutor) {
        CacheBuilder<K, FileProxy> cacheBuilder = CacheBuilder.newBuilder()
                .maximumWeight(localFileCacheStorage.getCapacityInKB()) // the in memory cache uses the cache entry weigh and maximum weight for the eviction policy
                .weigher((FileKey key, FileProxy fp) -> {
                    Long fileEstimatedSize =  fp.estimateSizeInBytes(false); // with caching we don't always check the latest size
                    if (fileEstimatedSize == null || fileEstimatedSize == 0L) {
                        return 1;
                    } else {
                        long sizeInKB = LocalFileCacheStorage.BYTES_TO_KB.apply(fileEstimatedSize);
                        return sizeInKB > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sizeInKB;
                    }
                })
                .removalListener(RemovalListeners.asynchronous(new CachedFileRemovalListener(), asyncRemovalExecutor));
        if (cacheConcurrency > 0) {
            cacheBuilder.concurrencyLevel(cacheConcurrency);
        }
        this.localCache = cacheBuilder.build(new RemoteFileLoader<>(localFileCacheStorage, keyToProxySupplier, localFileWriterExecutor));

    }

    public FileProxy getCachedFileEntry(K cachedFileKey, boolean forceRefresh) throws FileNotFoundException {
        if (forceRefresh) {
            localCache.invalidate(cachedFileKey);
        }
        try {
            return localCache.get(cachedFileKey);
        } catch (ExecutionException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    public Optional<FileProxy> getOptionalCachedFileEntry(K cachedFileKey, boolean forceRefresh) {
        try {
            return Optional.of(getCachedFileEntry(cachedFileKey, forceRefresh));
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

}

