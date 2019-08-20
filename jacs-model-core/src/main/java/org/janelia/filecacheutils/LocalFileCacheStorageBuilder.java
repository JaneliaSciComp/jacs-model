package org.janelia.filecacheutils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
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
                LOG.info("Initialize cache storage at {}", localFileCacheStorage.getLocalFileCacheDir());
                Files.walkFileTree(localFileCacheStorage.getLocalFileCacheDir(), new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        localFileCacheStorage.cacheFile(file, localFileCacheStorage.getFileSizeInKB(file));
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (!dir.equals(localFileCacheStorage.getLocalFileCacheDir())) {
                            File[] dirList = dir.toFile().listFiles();
                            if (dirList.length == 0) {
                                Files.delete(dir);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                LOG.info("Finished initializing current cache storage with {} entries - it currently is {}% full ({}KB / {}KB)",
                        localFileCacheStorage.size(),
                        localFileCacheStorage.getUsageAsPercentage(),
                        localFileCacheStorage.getCurrentSizeInKB(),
                        localFileCacheStorage.getCapacityInKB());
            } catch (Exception e) {
                // log this but don't rethrow it
                LOG.warn("Error while trying to calculate current cache storage size", e);
            }
        });
    }
}
