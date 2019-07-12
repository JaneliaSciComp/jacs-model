package org.janelia.filecacheutils;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Supplier;

public class CachedFileProxy implements FileProxy {
    private static final Logger LOG = LoggerFactory.getLogger(CachedFileProxy.class);
    private static final Set<String> DOWNLOADING_FILES = new ConcurrentSkipListSet<>();

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
    public InputStream openContentStream() {
        if (Files.exists(localFilePath)) {
            try {
                Files.setLastModifiedTime(localFilePath, FileTime.fromMillis(System.currentTimeMillis()));
                return Files.newInputStream(localFilePath);
            } catch (IOException e) {
                LOG.error("Error reading {}", localFilePath, e);
                throw new IllegalStateException(e);
            }
        } else {
            if (fileProxy == null) {
                fileProxy = fileProxySupplier.get();
            }
            InputStream contentStream = fileProxy.openContentStream();
            if (contentStream == null) {
                return null;
            }
            long estimatedSize = fileProxy.estimateSizeInBytes().orElse(0L);
            if (LocalFileCacheStorage.BYTES_TO_KB.apply(estimatedSize) + localFileCacheStorage.getCurrentSizeInKB() > localFileCacheStorage.getCapacityInKB() || !DOWNLOADING_FILES.add(localFilePath.toString())) {
                // if caching this file will exceed the size of cache or
                // some other thread is already downloading this file
                // simply return the stream directly without any attempt to store it in the local cache
                return contentStream;
            }
            try {
                // download remote file
                makeDownloadDir();
                Path downloadingLocalFilePath = getDownloadingLocalFilePath();
                OutputStream downloadingLocalFileStream = new FileOutputStream(downloadingLocalFilePath.toFile());
                return new TeeInputStream(contentStream, downloadingLocalFileStream, false) {
                    @Override
                    protected void afterRead(int n) throws IOException {
                        try {
                            super.afterRead(n);
                        } finally {
                            if (n == -1) {
                                try {
                                    downloadingLocalFileStream.close();
                                } catch (Exception e) {
                                    // Instead of ignoring this, it's a warning because on Windows file cannot be moved if it is in use
                                    LOG.warn("Error closing downloading local file {}", downloadingLocalFilePath, e);
                                }
                                persistToLocalCache();
                            }
                        }
                    }

                    @Override
                    public void close() throws IOException {
                        try {
                            super.close();
                        } finally {
                            try {
                                downloadingLocalFileStream.close();
                            } catch (Exception e) {
                                // Instead of ignoring this, it's a warning because on Windows file cannot be moved if it is in use
                                LOG.warn("Error closing downloading local file {}", downloadingLocalFilePath, e);
                            }
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
                                // this is safe to remove because this block should be executed only if no other thread
                                // was downloading this file at the time of this was requested
                                DOWNLOADING_FILES.remove(localFilePath.toString());
                                // update the cache size
                                localFileCacheStorage.updateCachedFiles(localFilePath, LocalFileCacheStorage.BYTES_TO_KB.apply(getCurrentSizeInBytes()));
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
            File localFile = localFilePath.toFile();
            localFile.setLastModified(System.currentTimeMillis());
            return localFile;
        } else {
            if (fileProxy == null) {
                fileProxy = fileProxySupplier.get();
            }
            InputStream contentStream = fileProxy.openContentStream();
            if (contentStream == null) {
                return null;
            }
            if (!DOWNLOADING_FILES.add(localFilePath.toString())) {
                // some other thread may also be downloading this file so in this case simply return the file
                // without trying to cache it locally again.
                // this is a bit risky because the download may fail but this entire method should be scrapped
                // and the calls to it should be refactored to use FileProxy instead of File
                return localFilePath.toFile();
            }
            // download remote file
            Path downloadingLocalFilePath = getDownloadingLocalFilePath();
            try {
                try {
                    makeDownloadDir();
                    Files.copy(contentStream, downloadingLocalFilePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    LOG.error("Error saving downloadable stream to {}", downloadingLocalFilePath, e);
                    throw new IllegalStateException(e);
                }
                try {
                    Files.move(downloadingLocalFilePath, localFilePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    localFileCacheStorage.updateCachedFiles(localFilePath, LocalFileCacheStorage.BYTES_TO_KB.apply(getCurrentSizeInBytes()));
                } catch (IOException e) {
                    LOG.error("Error moving downloaded file {} to {}", downloadingLocalFilePath, localFilePath, e);
                    throw new IllegalStateException(e);
                }
            } finally {
                DOWNLOADING_FILES.remove(localFilePath.toString());
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
            if (cacheDirCanonicalPath == null || !localCanonicalPath.toString().startsWith(cacheDirCanonicalPath.toString())) {
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
        Path downloadingLocalFilePath = getDownloadingLocalFilePath();
        try {
            Files.deleteIfExists(downloadingLocalFilePath);
        } catch (IOException e) {
            LOG.warn("Error deleting temp cached file {}", downloadingLocalFilePath, e);
        }
        localFileCacheStorage.updateCachedFiles(localFilePath, -LocalFileCacheStorage.BYTES_TO_KB.apply(sizeInBytes));
        return bresult;
    }

    private Path getDownloadingLocalDir() {
        return localFilePath.getParent();
    }

    private Path getDownloadingLocalFilePath() {
        return Paths.get(localFilePath.toString() + ".downloading");
    }
}
