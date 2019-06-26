package org.janelia.cacheutils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFileCacheStorage {
    private static final Logger LOG = LoggerFactory.getLogger(LocalFileCacheStorage.class);

    private final Path localFileCacheDir;
    private long capacityInKB;
    private AtomicLong currentSizeInKB;

    public LocalFileCacheStorage(Path localFileCacheDir, long capacityInKB) {
        this.localFileCacheDir = localFileCacheDir;
        this.capacityInKB = capacityInKB;
        this.currentSizeInKB = new AtomicLong(getLocalFileCacheStorageSizeInKB());
    }

    private long getLocalFileCacheStorageSizeInKB() {
        try {
            return Files.walk(localFileCacheDir)
                    .filter(fp -> Files.isRegularFile(fp, LinkOption.NOFOLLOW_LINKS))
                    .map(fp -> getFileSizeInKB(fp))
                    .reduce(0L, (s1, s2) -> s1 + s2);
        } catch (IOException e) {
            LOG.warn("Error initializing local file cache size for {}", localFileCacheDir, e);
            throw new IllegalStateException(e);
        }
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
            final long len = fp.toFile().length();
            if ((len % 1024) > 0) {
                return len / 1024 + 1;
            } else {
                return len / 1024;
            }
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
}
