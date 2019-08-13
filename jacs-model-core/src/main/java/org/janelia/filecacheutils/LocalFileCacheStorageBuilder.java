package org.janelia.filecacheutils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocalFileCacheStorage builder.
 */
public class LocalFileCacheStorageBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(LocalFileCacheStorageBuilder.class);

    private Path cacheDir;
    private long capacityInKB;
    private long maxFileSizeInKB;
    private boolean disabled;
    private ExecutorService executorService;

    public LocalFileCacheStorageBuilder withCapacityInKB(long capacityInKB) {
        this.capacityInKB = capacityInKB;
        return this;
    }

    public LocalFileCacheStorageBuilder withMaxFileSizeInKB(long maxFileSizeInKB) {
        this.maxFileSizeInKB = maxFileSizeInKB;
        return this;
    }

    public LocalFileCacheStorageBuilder withCacheDir(Path cacheDir) {
        this.cacheDir = cacheDir;
        return this;
    }

    public LocalFileCacheStorageBuilder withExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    public LocalFileCacheStorageBuilder withDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public LocalFileCacheStorage build() {
        Preconditions.checkArgument(cacheDir != null, "Cache base directory must be provided");
        prepareCacheDir();
        LocalFileCacheStorage localFileCacheStorage = new LocalFileCacheStorage(cacheDir, capacityInKB, maxFileSizeInKB);
        localFileCacheStorage.setDisabled(disabled);
        initializeCache(localFileCacheStorage);
        return localFileCacheStorage;
    }

    private void prepareCacheDir() {
        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            LOG.error("Error creating local cache directory: {}", cacheDir, e);
            throw new IllegalStateException(e);
        }
    }

    private void initializeCache(LocalFileCacheStorage localFileCacheStorage) {
        ExecutorService initializeExecutor;
        if (executorService != null) {
            initializeExecutor = executorService;
        } else {
            initializeExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                    .setNameFormat("local-file-cache-load-thread")
                    .setDaemon(true)
                    .build());
        }
        // this method doesn't actually load anything in memory cache
        // it only calculates the size of the current cache storage and
        // it performs some clean up for files that have not been touched in the last 24h.
        initializeExecutor.submit(() -> {
            try {
                Files.walk(localFileCacheStorage.getLocalFileCacheDir())
                        .sorted(Comparator.reverseOrder())
                        .filter(p -> !p.equals(localFileCacheStorage.getLocalFileCacheDir()))
                        .forEach(p -> {
                            try {
                                if (Files.isDirectory(p)) {
                                    File d = p.toFile();
                                    File[] dirList = d.listFiles();
                                    if (dirList.length == 0) {
                                        if (!d.delete()) {
                                            LOG.debug("Could not delete dir {}", p);
                                        }
                                    }
                                } else if (Files.isRegularFile(p)) {
                                    // since the cache eviction only applies to files that are present in the in-memory cache
                                    // and does not do anything about the files that are only on the file system,
                                    // for now we look at files that have not been touched in the last 24h or
                                    // at the files that exceed the new max acceptable size for caching
                                    // and we remove them.
                                    File f = p.toFile();
                                    if (f.lastModified() < System.currentTimeMillis() - 24 * 3600 * 1000L || !localFileCacheStorage.isBytesSizeAcceptable(f.length())) {
                                        if (!f.delete()) {
                                            LOG.debug("Could not delete file {}", f);
                                        }
                                    } else {
                                        localFileCacheStorage.updateCachedFiles(localFileCacheStorage.getFileSizeInKB(p));
                                    }
                                }
                            } catch (Exception e) {
                                LOG.error("Error handling {} while calculating cache storage size", p, e);
                            }
                        });
                LOG.info("Finished initializing current cache storage which currently is {}% full ({}KB / {}KB)",
                        localFileCacheStorage.getUsageAsPercentage(), localFileCacheStorage.getCurrentSizeInKB(), localFileCacheStorage.getCapacityInKB());
            } catch (Exception e) {
                // log this but don't rethrow it
                LOG.warn("Error while trying to calculate current cache storage size", e);
            }
        });
    }
}
