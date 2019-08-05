package org.janelia.filecacheutils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final NavigableSet<Path> allFilesFromCache;
    private long capacityInKB;
    private AtomicLong currentSizeInKB;

    public LocalFileCacheStorage(Path localFileCacheDir, long capacityInKB) {
        this.localFileCacheDir = localFileCacheDir;
        this.capacityInKB = capacityInKB;
        this.currentSizeInKB = new AtomicLong(0);
        this.allFilesFromCache = new ConcurrentSkipListSet<Path>((p1, p2) -> {
            try {
                long s1;
                long s2;
                long t1;
                long t2;
                if (Files.exists(p1)) {
                    s1 = Files.size(p1);
                    t1 = Files.getLastModifiedTime(p1).toMillis();
                } else {
                    s1 = 0;
                    t1 = 0;
                }
                if (Files.exists(p2)) {
                    s2 = Files.size(p2);
                    t2 = Files.getLastModifiedTime(p2).toMillis();
                } else {
                    s2 = 0;
                    t2 = 0;
                }
                if (s1 == 0) {
                    return -1;
                } else if (s2 == 0) {
                    return 1;
                } else if (s1 == s2) {
                    // older file before newer one
                    if (t1 < t2) {
                        return -1;
                    } else if (t1 > t2) {
                        return 1;
                    } else {
                        return 0;
                    }
                } else if (s1 < s2) {
                    return 1;
                } else {
                    return -1;
                }
            } catch (IOException e) {
                LOG.error("Error comparing the sizes of {} and {}", p1, p2, e);
                throw new IllegalStateException(e);
            }
        });
        init();
    }

    private void init() {
        try {
            Files.createDirectories(localFileCacheDir);
        } catch (IOException e) {
            LOG.error("Error creating local cache directory: {}", localFileCacheDir, e);
            throw new IllegalStateException(e);
        }
        initializeCachedFiles();
    }

    private void initializeCachedFiles() {
        try {
            Files.walk(localFileCacheDir)
                    .sorted(Comparator.reverseOrder())
                    .filter(p -> !p.equals(localFileCacheDir))
                    .forEach(p -> {
                        if (Files.isDirectory(p)) {
                            File f = p.toFile();
                            File[] dirList = f.listFiles();
                            if (dirList.length == 0) {
                                f.delete();
                            }
                        } else if (Files.isRegularFile(p)) {
                            // right now the cache eviction does not apply to file that already exist on the file system
                            // to prevent continuous accumulation of these for now simply remove files older than 2h
                            if (p.toFile().lastModified() < System.currentTimeMillis() - 2 * 3600 * 1000L) {
                                p.toFile().delete();
                            } else {
                                updateCachedFiles(p, getFileSizeInKB(p));
                            }
                        }
                    });
        } catch (IOException e) {
            // log this but don't rethrow it
            LOG.warn("Error while trying to cleanup cache sub-directories", e);
        }
    }

    Stream<Path> walkCachedFiles() {
        try {
            return Files.walk(localFileCacheDir)
                    .filter(fp -> Files.isRegularFile(fp, LinkOption.NOFOLLOW_LINKS))
                    ;
        } catch (IOException e) {
            LOG.warn("Error traversing local cache directory {}", localFileCacheDir, e);
            throw new IllegalStateException(e);
        }
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

    long getFileSizeInKB(Path fp) {
        if (Files.exists(fp)) {
            return BYTES_TO_KB.apply(fp.toFile().length());
        } else {
            return 0;
        }
    }

    void updateCachedFiles(Path file, long size) {
        Function<Path, Boolean> op;
        if (size > 0) {
            op = p -> allFilesFromCache.add(p);
        } else {
            op = p -> allFilesFromCache.remove(p);
        }
        if (op.apply(file)) {
            currentSizeInKB.accumulateAndGet(size, (v1, v2) -> {
                long v = v1 + v2;
                return v < 0 ? 0 : v;
            });
        }
    }

    public void clear() {
        try {
            Files.walkFileTree(localFileCacheDir, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
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
