package org.janelia.filecacheutils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the storage used for local file caching.
 */
public class LocalFileCacheStorage {
    private static final int LOW_WATERMARK = 70;
    private static final int HIGH_WATERMARK = 80;
    private static final Logger LOG = LoggerFactory.getLogger(LocalFileCacheStorage.class);

    private static class CachedFileEntry {

        private final String fileName;
        private final long fileSizeInKB;
        private long lastUpdatedTimestamp;

        private CachedFileEntry(String fileName, long fileSizeInKB, long lastUpdatedTimestamp) {
            this.fileName = fileName;
            this.fileSizeInKB = fileSizeInKB;
            this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            CachedFileEntry that = (CachedFileEntry) o;

            return new EqualsBuilder()
                    .append(fileName, that.fileName)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(fileName)
                    .toHashCode();
        }
    }

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
    private final NavigableMap<CachedFileEntry, CachedFileEntry> cachedFiles;
    private long capacityInKB;
    private long maxCachedFileSizeInKB;
    private final AtomicLong currentSizeInKB; // cache size in KB
    private final AtomicInteger currentSize; // number of entries in the cache
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
        this.currentSize = new AtomicInteger(0);
        this.cachedFiles = new ConcurrentSkipListMap<>((e1, e2) -> {
            long t1 = e1.lastUpdatedTimestamp;
            long t2 = e2.lastUpdatedTimestamp;
            // we want the key to be actually the name only but
            // since we want to use some sort of LRU for eviction we have to use the timestamp as well
            // to guarantee that the same name always give you the same entry if the names are equal
            // the result of the comparison shows equality
            int nameComparison = e1.fileName.compareTo(e2.fileName);
            if (nameComparison == 0) {
                return 0;
            } else {
                // then if the names are different compare the timestamps
                // older file before newer one
                if (t1 < t2) {
                    return -1;
                } else if (t1 > t2) {
                    return 1;
                } else {
                    return nameComparison;
                }
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

    public synchronized void setCapacityInKB(long capacityInKB) {
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
                CachedFileEntry lookupEntry = new CachedFileEntry(p.toString(), sizeInKB, p.toFile().lastModified());
                CachedFileEntry removedEntry = cachedFiles.remove(lookupEntry);
                if (removedEntry != null) {
                    deleteCachedFile(removedEntry);
                }
            };
        } else {
            handler = p -> {};
        }
        handler.accept(file);
    }

    void cacheFile(Path file, long sizeInKB) {
        LOG.trace("Caching {} ({} KB) ({} entries)", file, sizeInKB, size());
        CachedFileEntry newCacheEntry = new CachedFileEntry(file.toString(), sizeInKB, file.toFile().lastModified());
        if (cachedFiles.put(newCacheEntry, newCacheEntry) == null) {
            currentSize.incrementAndGet();
            currentSizeInKB.accumulateAndGet(sizeInKB, (v1, v2) -> {
                long v = v1 + v2;
                return v < 0 ? 0 : v;
            });
            LOG.debug("Cached {} ({} KB) ({} entries) - usage {}%", file, sizeInKB, size(), getUsageAsPercentage());
        }
    }

    private synchronized void removeUntilSpaceIsAvalable() {
        int usage = getUsageAsPercentage();
        if (usage > HIGH_WATERMARK) {
            double updatedSizeInKB = currentSizeInKB.doubleValue();
            LOG.info("Local cache usage reached the high water mark -> {}", usage);
            Collection<CachedFileEntry> cacheEntriesToBeDeleted = new LinkedList<>();
            // this loop simply iterates and collect entries to be deleted until the low watermark is reached
            for (CachedFileEntry cachedFileEntry : cachedFiles.navigableKeySet()) {
                cacheEntriesToBeDeleted.add(cachedFileEntry);
                updatedSizeInKB -= cachedFileEntry.fileSizeInKB;
                int usageAfterRemoval  = (int) (updatedSizeInKB / capacityInKB * 100.);
                if (usageAfterRemoval <= LOW_WATERMARK) {
                    break;
                }
            }
            // now we are actually deleting
            cacheEntriesToBeDeleted.forEach(cachedFileEntry -> {
                CachedFileEntry removedEntry = cachedFiles.remove(cachedFileEntry);
                if (removedEntry != null) {
                    deleteCachedFile(removedEntry);
                }
            });
            LOG.info("Local cache storage has {} entries and it is {}% full ({}KB / {}KB)",
                    size(),
                    getUsageAsPercentage(),
                    getCurrentSizeInKB(),
                    getCapacityInKB());
        }
    }

    private synchronized void deleteCachedFile(CachedFileEntry cachedFileEntry) {
        try {
            Path p = Paths.get(cachedFileEntry.fileName);
            if (Files.deleteIfExists(p)) {
                LOG.info("Removed {} ({} KB last used on {}) from local storage cache", cachedFileEntry.fileName, new Date(cachedFileEntry.lastUpdatedTimestamp), cachedFileEntry.fileSizeInKB);
                currentSize.decrementAndGet();
                currentSizeInKB.accumulateAndGet(-cachedFileEntry.fileSizeInKB, (v1, v2) -> {
                    long v = v1 + v2;
                    return v < 0 ? 0 : v;
                });
            }
        } catch (Exception e) {
            LOG.warn("Error trying to remove {} from local cache", cachedFileEntry, e);
        }
    }

    int size() {
        return currentSize.get();
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

    synchronized InputStream openLocalCachedFile(Path localFilePath) {
        try {
            Path cachedFilePath = getLocalCachedFile(localFilePath);
            if (cachedFilePath != null) {
                return Files.newInputStream(cachedFilePath);
            }
        } catch (IOException e) {
            LOG.debug("Error getting the content of the locally cached file {}", localFilePath, e);
        }
        return null;
    }

    synchronized Path getLocalCachedFile(Path filePath) {
        long currentTime = System.currentTimeMillis();
        CachedFileEntry lookupEntry = new CachedFileEntry(filePath.toString(), 0, System.currentTimeMillis());
        // to touch the cache entry we remove it and reinsert it so that the timestamp gets updated
        CachedFileEntry cachedFileEntry = cachedFiles.remove(lookupEntry);
        if (cachedFileEntry != null && Files.exists(filePath)) {
            cachedFileEntry.lastUpdatedTimestamp = currentTime;
            try {
                Files.setLastModifiedTime(filePath, FileTime.fromMillis(currentTime));
                cachedFiles.put(cachedFileEntry, cachedFileEntry);
                return filePath;
            } catch (IOException e) {
                LOG.debug("Error touching locally cached file {}", filePath, e);
            }
        }
        return null;
    }
}
