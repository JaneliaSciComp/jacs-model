package org.janelia.filecacheutils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the storage used for local file caching.
 */
public class LocalFileCacheStorage {
    private static final int LOW_WATERMARK = 75;
    private static final int HIGH_WATERMARK = 90;
    private static final Logger LOG = LoggerFactory.getLogger(LocalFileCacheStorage.class);

    static Function<Long, Long> BYTES_TO_KB = (b) -> {
        if (b > 0L) {
            if ((b % 1024) > 0) {
                return b / 1024 + 1;
            } else {
                return b / 1024;
            }
        } else {
            return 0L;
        }
    };

    private final Path localFileCacheDir;
    private final NavigableSet<Path> cachedFiles;
    private long capacityInKB;
    private long maxCachedFileSizeInKB;
    private AtomicLong currentSizeInKB;
    private boolean disabled;

    /**
     * @param localFileCacheDir cache directory
     * @param capacityInKB cache capacity in kiloBytes
     */
    LocalFileCacheStorage(Path localFileCacheDir, long capacityInKB, long maxCachedFileSizeInKB) {
        Preconditions.checkState(Files.exists(localFileCacheDir), "Cache directory " + localFileCacheDir + " must exist");
        this.localFileCacheDir = localFileCacheDir;
        this.capacityInKB = capacityInKB;
        this.maxCachedFileSizeInKB = maxCachedFileSizeInKB;
        this.currentSizeInKB = new AtomicLong(0);
        this.cachedFiles = new ConcurrentSkipListSet<>((p1, p2) -> {
            try {
                long t1;
                long t2;
                if (Files.exists(p1)) {
                    t1 = Files.getLastModifiedTime(p1).toMillis();
                } else {
                    t1 = 0;
                }
                if (Files.exists(p2)) {
                    t2 = Files.getLastModifiedTime(p2).toMillis();
                } else {
                    t2 = 0;
                }
                // older file before newer one
                if (t1 < t2) {
                    return -1;
                } else if (t1 > t2) {
                    return 1;
                } else {
                    return p1.compareTo(p2);
                }
            } catch (IOException e) {
                LOG.error("Error comparing {} and {}", p1, p2, e);
                throw new IllegalStateException(e);
            }
        });
    }

    public Path getLocalFileCacheDir() {
        return localFileCacheDir;
    }

    public long getCurrentSizeInKB() {
        return currentSizeInKB.get();
    }

    public long getCapacityInKB() {
        return capacityInKB;
    }

    public void setCapacityInKB(long capacityInKB) {
        this.capacityInKB = capacityInKB;
    }

    public long getMaxCachedFileSizeInKB() {
        return maxCachedFileSizeInKB;
    }

    public void setMaxCachedFileSizeInKB(long maxCachedFileSizeInKB) {
        this.maxCachedFileSizeInKB = maxCachedFileSizeInKB;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isBytesSizeAcceptable(long sizeInBytes) {
        if (disabled || sizeInBytes <= 0) {
            return false;
        } else {
            if (maxCachedFileSizeInKB <= 0) {
                return true;
            } else {
                long sizeInKB = BYTES_TO_KB.apply(sizeInBytes);
                return sizeInKB < maxCachedFileSizeInKB && sizeInKB + getCurrentSizeInKB() < capacityInKB;
            }
        }
    }

    public int getUsageAsPercentage() {
        if (capacityInKB == 0) {
            return 0;
        } else {
            return (int) ((currentSizeInKB.doubleValue() / (double) capacityInKB) * 100);
        }
    }

    long getFileSizeInKB(Path fp) {
        if (Files.exists(fp)) {
            return BYTES_TO_KB.apply(fp.toFile().length());
        } else {
            return 0;
        }
    }

    void updateCachedFiles(Path file, long sizeInKB) {
        Consumer<Path> handler;
        if (sizeInKB > 0) {
            // a file is added
            handler = p -> {
                // first check if space is available
                removeUntilSpaceIsAvalable();
                cacheFile(file, sizeInKB);
            };
        } else if (sizeInKB < 0) {
            // a file is removed
            handler = p -> {
                if (deleteCachedFile(p)) {
                    cachedFiles.remove(p);
                }
            };
        } else {
            handler = p -> {};
        }
        handler.accept(file);
    }

    void cacheFile(Path file, long sizeInKB) {
        LOG.trace("Caching {} ({} KB) ({} entries)", file, sizeInKB, size());
        if (cachedFiles.add(file)) {
            LOG.debug("Cached {} ({} KB) ({} entries) - usage {}%", file, sizeInKB, size(), getUsageAsPercentage());
            currentSizeInKB.accumulateAndGet(sizeInKB, (v1, v2) -> {
                long v = v1 + v2;
                return v < 0 ? 0 : v;
            });
        }
    }

    private synchronized void removeUntilSpaceIsAvalable() {
        int usage = getUsageAsPercentage();
        if (usage > HIGH_WATERMARK) {
            LOG.info("Local cache usage reached the high water mark -> {}", usage);
            Collection<Path> deletedFiles = new LinkedList<>();
            for (Path cachedFile : cachedFiles) {
                if (deleteCachedFile(cachedFile)) {
                    deletedFiles.add(cachedFile);
                    int usageAfterRemoval = getUsageAsPercentage();
                    if (usageAfterRemoval <= LOW_WATERMARK) {
                        break;
                    }
                }
            }
            cachedFiles.removeAll(deletedFiles);
            LOG.info("Local cache storage has {} entries and it is {}% full ({}KB / {}KB)",
                    size(),
                    getUsageAsPercentage(),
                    getCurrentSizeInKB(),
                    getCapacityInKB());
        }
    }

    private synchronized boolean deleteCachedFile(Path p) {
        try {
            File f = p.toFile();
            if (f.exists()) {
                long fSize = f.length();
                if (f.delete()) {
                    LOG.info("Removed {} from local storage cache", f);
                    currentSizeInKB.accumulateAndGet(-BYTES_TO_KB.apply(fSize), (v1, v2) -> {
                        long v = v1 + v2;
                        return v < 0 ? 0 : v;
                    });
                    return true;
                }
            }
        } catch (Exception e) {
            LOG.warn("Error trying to remove {} from local cache", p, e);
        }
        return false;
    }

    int size() {
        return cachedFiles.size();
    }

    public void clear() {
        try {
            Files.walkFileTree(localFileCacheDir, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            currentSizeInKB.set(0);
        } catch (IOException e) {
            LOG.error("Error while cleaning up the cache directory {}", localFileCacheDir, e);
        }
    }
}
