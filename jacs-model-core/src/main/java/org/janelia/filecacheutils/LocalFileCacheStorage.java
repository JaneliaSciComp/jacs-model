package org.janelia.filecacheutils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
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
    private long capacityInKB;
    private AtomicLong currentSizeInKB;

    public LocalFileCacheStorage(Path localFileCacheDir, long capacityInKB) {
        this.localFileCacheDir = localFileCacheDir;
        this.capacityInKB = capacityInKB;
        init();
    }

    private void init() {
        try {
            Files.createDirectories(localFileCacheDir);
        } catch (IOException e) {
            LOG.error("Error creating local cache directory: {}", localFileCacheDir, e);
            throw new IllegalStateException(e);
        }
        deleteEmptySubDirs();
        this.currentSizeInKB = new AtomicLong(getLocalFileCacheStorageSizeInKB());
    }

    private void deleteEmptySubDirs() {
        try {
            Files.walk(localFileCacheDir).sorted(Comparator.reverseOrder())
                    .filter(p -> Files.isDirectory(p))
                    .filter(p -> !p.equals(localFileCacheDir))
                    .map(Path::toFile)
                    .forEach(f -> {
                        File[] dirList = f.listFiles();
                        if (dirList != null && dirList.length == 0) {
                            f.delete();
                        }
                    });
        } catch (IOException e) {
            // log this but don't rethrow it
            LOG.warn("Error while trying to cleanup cache sub-directories", e);
        }
    }

    private long getLocalFileCacheStorageSizeInKB() {
        return walkCachedFiles()
                .map(fp -> getFileSizeInKB(fp))
                .reduce(0L, (s1, s2) -> s1 + s2);
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

    void updateCurrentSizeInKB(long s) {
        currentSizeInKB.accumulateAndGet(s, (v1, v2) -> {
            long v = v1 + v2;
            return v < 0 ? 0 : v;
        });
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
