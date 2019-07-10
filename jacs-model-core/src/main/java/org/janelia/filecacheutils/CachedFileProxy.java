package org.janelia.filecacheutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.io.input.TeeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedFileProxy implements FileProxy {
    private static final Logger LOG = LoggerFactory.getLogger(CachedFileProxy.class);

    private final Path localFilePath;
    private final Supplier<FileProxy> fileProxySupplier;
    private final LocalFileCacheStorage localFileCacheStorage;
    private FileProxy fileProxy;

    CachedFileProxy(Path localFilePath, Supplier<FileProxy> fileProxySupplier, LocalFileCacheStorage localFileCacheStorage) {
        this.localFileCacheStorage = localFileCacheStorage;
        this.localFilePath = localFilePath;
        this.fileProxySupplier = fileProxySupplier;
        this.fileProxy = null;
    }

    @Override
    public String getFileId() {
        if (localFilePath != null) {
            return localFilePath.toString();
        } else {
            return fileProxySupplier.get().getFileId();
        }
    }

    @Override
    public Optional<Long> estimateSizeInBytes() {
        long currentSize = getCurrentSizeInBytes();
        if (currentSize > 0) {
            return Optional.of(currentSize);
        } else if (fileProxy == null) {
            fileProxy = fileProxySupplier.get();
        }
        return fileProxy.estimateSizeInBytes();
    }

    private long getCurrentSizeInBytes() {
        return getFileSize(localFilePath) + getFileSize(getDownloadingLocalFilePath());
    }

    private long getFileSize(Path fp) {
        long fSize;
        if (Files.exists(fp)) {
            try {
                fSize = Files.size(fp);
            } catch (IOException e) {
                LOG.error("Error reading file size for {}", fp, e);
                fSize = 0L;
            }
        } else {
            fSize = 0L;
        }
        return fSize;
    }

    @Override
    public InputStream getContentStream() {
        if (Files.exists(localFilePath)) {
            try {
                return Files.newInputStream(localFilePath);
            } catch (IOException e) {
                LOG.error("Error reading {}", localFilePath, e);
                throw new IllegalStateException(e);
            }
        } else {
            try {
                if (fileProxy == null) {
                    fileProxy = fileProxySupplier.get();
                }
                InputStream contentStream = fileProxy.getContentStream();
                if (contentStream == null) {
                    return null;
                }
                // download remote file
                makeDownloadDir();
                Path downloadingLocalFilePath = getDownloadingLocalFilePath();
                FileOutputStream downloadingLocalFileStream = new FileOutputStream(downloadingLocalFilePath.toFile());
                return new TeeInputStream(contentStream, downloadingLocalFileStream, false) {
                    @Override
                    protected void afterRead(int n) throws IOException {
                        try {
                            super.afterRead(n);
                        } finally {
                            if (n == -1) {
                                downloadingLocalFileStream.close();
                                persistToLocalCache();
                            }
                        }
                    }

                    @Override
                    public void close() throws IOException {
                        try {
                            super.close();
                        } finally {
                            downloadingLocalFileStream.close();
                            persistToLocalCache();
                        }
                    }

                    private void persistToLocalCache() {
                        try {
                            Path localDir = localFilePath.getParent();
                            if (localDir != null && Files.notExists(localDir)) {
                                Files.createDirectories(localDir);
                            }
                            if (Files.notExists(localFilePath) && Files.exists(downloadingLocalFilePath)) {
                                long downloadedFileSize = Files.size(downloadingLocalFilePath);
                                if (downloadedFileSize / 1024 < localFileCacheStorage.getCapacityInKB()) {
                                    Files.move(downloadingLocalFilePath, localFilePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                                }
                            }
                        } catch (IOException e) {
                            LOG.error("Error moving downloaded file {} to local cache file {}", downloadingLocalFilePath, localFilePath, e);
                        } finally {
                            try {
                                Files.deleteIfExists(downloadingLocalFilePath);
                            } catch (IOException e) {
                                LOG.debug("Error deleting {}", downloadingLocalFilePath, e);
                            } finally {
                                updateLocalCacheSizeInKB(LocalFileCacheStorage.BYTES_TO_KB.apply(getCurrentSizeInBytes()));
                            }
                        }
                    }
                };
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public File getLocalFile() {
        if (Files.exists(localFilePath)) {
            return localFilePath.toFile();
        } else {
            // download remote file
            try {
                InputStream contentStream = fileProxySupplier.get().getContentStream();
                if (contentStream == null) {
                    return null;
                }
                makeDownloadDir();
                Files.copy(contentStream, getDownloadingLocalFilePath(), StandardCopyOption.REPLACE_EXISTING);
                updateLocalCacheSizeInKB(LocalFileCacheStorage.BYTES_TO_KB.apply(getCurrentSizeInBytes()));
            } catch (IOException e) {
                LOG.error("Error saving downloadable stream to {}", getDownloadingLocalFilePath(), e);
                throw new IllegalStateException(e);
            }
            try {
                Files.move(getDownloadingLocalFilePath(), localFilePath, StandardCopyOption.REPLACE_EXISTING);
                updateLocalCacheSizeInKB(LocalFileCacheStorage.BYTES_TO_KB.apply(getCurrentSizeInBytes()));
            } catch (IOException e) {
                LOG.error("Error moving downloaded file {} to {}", getDownloadingLocalFilePath(), localFilePath, e);
                throw new IllegalStateException(e);
            }
        }
        return null;
    }

    private void makeDownloadDir() {
        try {
            Path downloadingDir = getDownloadingLocalDir();
            if (downloadingDir != null) {
                Files.createDirectories(downloadingDir);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean deleteProxy() {
        long sizeInBytes = getCurrentSizeInBytes();
        try {
            Path localCanonicalPath = localFilePath.toRealPath();
            Path cacheDirCanonicalPath = localFileCacheStorage.getLocalFileCacheDir().toRealPath();
            if (cacheDirCanonicalPath == null || !cacheDirCanonicalPath.toString().startsWith(localCanonicalPath.toString())) {
                LOG.info("Local file name {}({}) does not appeared to be located inside the cache dir {}({})",
                        localFilePath, localCanonicalPath, localFileCacheStorage.getLocalFileCacheDir(), cacheDirCanonicalPath);
                return false;
            }
        } catch (IOException e) {
            LOG.error("Error getting canonical path(s) for {} and/or {}", localFilePath, localFileCacheStorage.getLocalFileCacheDir(), e);
        }
        boolean bresult;
        try {
            bresult = Files.deleteIfExists(localFilePath);
        } catch (IOException e) {
            LOG.warn("Error deleting locally cached file {}", localFilePath, e);
            bresult = false;
        }
        try {
            Files.deleteIfExists(getDownloadingLocalFilePath());
        } catch (IOException e) {
            LOG.warn("Error deleting temp cached file {}", getDownloadingLocalFilePath(), e);
        }
        updateLocalCacheSizeInKB(-LocalFileCacheStorage.BYTES_TO_KB.apply(sizeInBytes));
        return bresult;
    }

    private void updateLocalCacheSizeInKB(long toAdd) {
        localFileCacheStorage.updateCurrentSizeInKB(toAdd);
    }

    private Path getDownloadingLocalDir() {
        return localFilePath.getParent();
    }

    private Path getDownloadingLocalFilePath() {
        return Paths.get(localFilePath.toString() + ".downloading");
    }
}
