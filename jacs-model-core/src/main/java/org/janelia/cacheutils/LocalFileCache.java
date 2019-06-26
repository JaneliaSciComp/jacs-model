package org.janelia.cacheutils;

import java.util.concurrent.Executor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListeners;

public class LocalFileCache {

    private final LoadingCache<CachedFileKey, CachedFile> localCache;

    public LocalFileCache(LocalFileCacheStorage localFileCacheStorage, RemoteFileRetriever remoteFileRetriever, Executor asyncRemovalExecutor) {
        this.localCache =
                CacheBuilder.newBuilder()
                        .concurrencyLevel(1)
                        .maximumWeight(localFileCacheStorage.getCapacityInKB())
                        .weigher((CachedFileKey key, CachedFile value) -> {
                            long sizeInKB = value.getSizeInKB();
                            return sizeInKB > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sizeInKB;
                        })
                        .removalListener(RemovalListeners.asynchronous(new CachedFileRemovalListener(), asyncRemovalExecutor))
                        .build(new RemoteFileLoader(localFileCacheStorage, remoteFileRetriever));
    }

}
