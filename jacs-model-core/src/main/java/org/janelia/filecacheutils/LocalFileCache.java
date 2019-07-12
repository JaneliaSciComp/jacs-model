package org.janelia.filecacheutils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFileCache<K extends FileKey> {
    private static final Logger LOG = LoggerFactory.getLogger(LocalFileCache.class);

    private final LocalFileCacheStorage localFileCacheStorage;
    private final LoadingCache<K, Optional<FileProxy>> localCache;

    public LocalFileCache(LocalFileCacheStorage localFileCacheStorage, FileKeyToProxySupplier<K> keyToProxySupplier, Executor asyncRemovalExecutor) {
        this.localFileCacheStorage = localFileCacheStorage;
        this.localCache =
                CacheBuilder.newBuilder()
                        .concurrencyLevel(2)
                        .maximumWeight(localFileCacheStorage.getCapacityInKB())
                        .weigher((FileKey key, Optional<FileProxy> value) -> {
                            long sizeInKB = LocalFileCacheStorage.BYTES_TO_KB.apply(value.flatMap(fp -> fp.estimateSizeInBytes()).orElse(1L));
                            return sizeInKB > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sizeInKB;
                        })
                        .removalListener(RemovalListeners.asynchronous(new CachedFileRemovalListener(), asyncRemovalExecutor))
                        .build(new RemoteFileLoader<>(localFileCacheStorage, keyToProxySupplier));
    }

    @Nullable
    public FileProxy getCachedFileEntry(K cachedFileKey, boolean forceRefresh) {
        if (forceRefresh) {
            localCache.invalidate(cachedFileKey);
        }
        try {
            FileProxy fp = localCache.get(cachedFileKey).orElse(null);
            if (localFileCacheStorage.getCurrentSizeInKB() > 0.85 * localFileCacheStorage.getCapacityInKB()) {
                Set<Path> locallyCached = localCache.asMap().keySet().stream().map(k -> k.getLocalPath(localFileCacheStorage)).collect(Collectors.toSet());
                localFileCacheStorage.walkCachedFiles()
                        .filter(p -> p.toFile().lastModified() < System.currentTimeMillis() - 600000L) // not touched in the last 10min
                        .filter(p -> !locallyCached.contains(p)) // only remove the files that are not in memory - the others will be handled by the cache eviction mechanism
                        .limit(20)
                        .forEach(p -> {
                            try {
                                LOG.debug("Delete {} from localcache dir", p);
                                Files.deleteIfExists(p);
                            } catch (IOException e) {
                                LOG.debug("Error removing {} from local cache", p, e);
                            }
                        });
            }
            return fp;
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }
}
