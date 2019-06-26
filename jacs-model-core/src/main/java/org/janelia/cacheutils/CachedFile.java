package org.janelia.cacheutils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import org.apache.commons.io.input.TeeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedFile {
    private static final Logger LOG = LoggerFactory.getLogger(CachedFile.class);

    private final LocalFileCacheStorage localFileCacheStorage;
    private final Path localFilePath;
    private final Path downloadingLocalFilePath;
    private InputStream downloadStream;

    CachedFile(Path localFilePath, Path downloadingLocalFilePath, LocalFileCacheStorage localFileCacheStorage) {
        this.localFilePath = localFilePath;
        this.downloadingLocalFilePath = downloadingLocalFilePath;
        this.localFileCacheStorage = localFileCacheStorage;
        this.downloadStream = null;
    }

    CachedFile(Path localFilePath, Path downloadingLocalFilePath, LocalFileCacheStorage localFileCacheStorage, InputStream downloadStream) {
        this.localFilePath = localFilePath;
        this.downloadingLocalFilePath = downloadingLocalFilePath;
        this.localFileCacheStorage = localFileCacheStorage;
        this.downloadStream = downloadStream;
    }

    public InputStream open(Function<Path, InputStream> streamSupplier) {
        if (downloadStream == null) {
            return streamSupplier.apply(localFilePath);
        } else {
            try {
                FileOutputStream tmpCacheFileStream = new FileOutputStream(downloadingLocalFilePath.toFile());
                return new TeeInputStream(downloadStream, tmpCacheFileStream, false) {
                    @Override
                    protected void afterRead(int n) throws IOException {
                        try {
                            super.afterRead(n);
                        } finally {
                            if (n == -1) {
                                tmpCacheFileStream.close();
                                Files.move(downloadingLocalFilePath, localFilePath);
                                downloadStream = null;
                                localFileCacheStorage.updateCurrentSizeInKB(getSizeInKB());
                            }
                        }
                    }
                };
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * @return the size in KB
     */
    long getSizeInKB() {
        return localFileCacheStorage.getFileSizeInKB(localFilePath) + localFileCacheStorage.getFileSizeInKB(downloadingLocalFilePath);
    }

    void delete() {
        long sizeInKb = getSizeInKB();
        try {
            Files.deleteIfExists(localFilePath);
        } catch (IOException e) {
            LOG.warn("Error deleting locally cached file {}", localFilePath, e);
        }
        try {
            Files.deleteIfExists(downloadingLocalFilePath);
        } catch (IOException e) {
            LOG.warn("Error deleting temp cached file {}", downloadingLocalFilePath, e);
        }
        localFileCacheStorage.updateCurrentSizeInKB(-sizeInKb);
    }

}
