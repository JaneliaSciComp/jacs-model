package org.janelia.filecacheutils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the storage used for local file caching.
 */
public class LocalFileCacheStorage {
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

    void updateCachedFiles(long size) {
        currentSizeInKB.accumulateAndGet(size, (v1, v2) -> {
            long v = v1 + v2;
            return v < 0 ? 0 : v;
        });
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
