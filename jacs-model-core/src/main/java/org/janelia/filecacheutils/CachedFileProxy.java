package org.janelia.filecacheutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cached file representation - implements a proxy for accessing content from a locally cached file.
 */
public class CachedFileProxy implements FileProxy {
    private static final Logger LOG = LoggerFactory.getLogger(CachedFileProxy.class);
    private static final Set<String> DOWNLOADING_FILES = new ConcurrentSkipListSet<>();

    private final Path localFilePath;
    private final Supplier<FileProxy> fileProxySupplier;
    private final LocalFileCacheStorage localFileCacheStorage;
    private final ExecutorService localFileWriterExecutor;
    private FileProxy fileProxy;

    CachedFileProxy(Path localFilePath, Supplier<FileProxy> fileProxySupplier, LocalFileCacheStorage localFileCacheStorage, ExecutorService localFileWriterExecutor) {
        this.localFileCacheStorage = localFileCacheStorage;
        this.localFilePath = localFilePath;
        this.fileProxySupplier = fileProxySupplier;
        this.localFileWriterExecutor = localFileWriterExecutor;
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
        try {
            return fp.toFile().length();
        } catch (Exception e) {
            LOG.error("Error reading file size for {}", fp, e);
            return 0L;
        }
    }

    @Override
    public InputStream openContentStream() {
        if (Files.exists(localFilePath)) {
            try {
                localFileCacheStorage.touch(localFilePath);
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
            if (!localFileCacheStorage.isBytesSizeAcceptable(estimatedSize) || !DOWNLOADING_FILES.add(localFilePath.toString())) {
                // if this file cannot be cached because it's either to large or caching it will exceed the cache capacity
                // or some other thread is already downloading this file
                // simply return the stream directly
                return contentStream;
            }
            try {
                // download remote file
                makeDownloadDir();
                Path downloadingLocalFilePath = getDownloadingLocalFilePath();
                OutputStream downloadingLocalFileStream = new FileOutputStream(downloadingLocalFilePath.toFile());
                // the method tees the remote stream to a local file image which is being written as the remote stream is being read.
                return new TeeInputStream(contentStream,
                        downloadingLocalFileStream,
                        (Void) -> {
                            try {
                                downloadingLocalFileStream.close();
                            } catch (Exception e) {
                                // Instead of ignoring this, it's a warning because on Windows file cannot be moved if it is in use
                                LOG.warn("Error closing downloading local file {}", downloadingLocalFilePath, e);
                            }
                            persistToLocalCache(downloadingLocalFilePath);
                        },
                        localFileWriterExecutor) {

                    @Override
                    protected void afterRead(int n) {
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
                                persistToLocalCache(downloadingLocalFilePath);
                            }
                        }
                    }

                    @Override
                    protected void handleIOException(IOException e) {
                        super.handleIOException(e);
                        // close the stream in case of an error
                        try {
                            downloadingLocalFileStream.close();
                        } catch (IOException ignore) {
                            // ignore
                        }
                        // in case of any exception while streaming the file
                        // remove the local cache image
                        try {
                            Files.deleteIfExists(downloadingLocalFilePath);
                        } catch (Exception deleteExc) {
                            LOG.debug("Error deleting {}", downloadingLocalFilePath, deleteExc);
                        }
                        try {
                            Files.deleteIfExists(localFilePath);
                        } catch (Exception deleteExc) {
                            LOG.debug("Error deleting {}", localFilePath, deleteExc);
                        }
                        DOWNLOADING_FILES.remove(localFilePath.toString());
                    }
                };
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void persistToLocalCache(Path downloadedFilePath) {
        try {
            Path localDir = localFilePath.getParent();
            if (localDir != null && Files.notExists(localDir)) {
                Files.createDirectories(localDir);
            }
            if (Files.notExists(localFilePath) && Files.exists(downloadedFilePath)) {
                Files.move(downloadedFilePath, localFilePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
        } catch (IOException e) {
            LOG.error("Error moving downloaded file {} to local cache file {}", downloadedFilePath, localFilePath, e);
        } finally {
            try {
                Files.deleteIfExists(downloadedFilePath);
            } catch (IOException e) {
                LOG.debug("Error deleting {}", downloadedFilePath, e);
            } finally {
                // this is safe to remove because this block should be executed only if no other thread
                // was downloading this file at the time of this was requested
                DOWNLOADING_FILES.remove(localFilePath.toString());
                // update the cache size
                localFileCacheStorage.updateCachedFiles(localFilePath, LocalFileCacheStorage.BYTES_TO_KB.apply(getCurrentSizeInBytes()));
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
                try {
                    contentStream.close();
                } catch (IOException ignore) {
                }
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
            if (Files.exists(localFilePath)) {
                // This can happen if user deleted the cache directory manually
                // and I don't think this should be an error
                Path localCanonicalPath = localFilePath.toRealPath();
                Path cacheDirCanonicalPath = localFileCacheStorage.getLocalFileCacheDir().toRealPath();
                if (!localCanonicalPath.toString().startsWith(cacheDirCanonicalPath.toString())) {
                    LOG.info("Local file name {}({}) does not appeared to be located inside the cache dir {}({})",
                            localFilePath, localCanonicalPath, localFileCacheStorage.getLocalFileCacheDir(), cacheDirCanonicalPath);
                    return false;
                }
            }
        } catch (IOException e) {
            LOG.error("Error getting canonical path(s) for {} and/or {}", localFilePath, localFileCacheStorage.getLocalFileCacheDir(), e);
        }
        Path downloadingLocalFilePath = getDownloadingLocalFilePath();
        try {
            // only really remove it from the cache if the downloading file is still present, because in that case
            // it is very likely something went wrong during the loading process
            if (!DOWNLOADING_FILES.contains(localFilePath.toString())) {
                // only do this if the downloading is done
                if (Files.deleteIfExists(downloadingLocalFilePath)) {
                    localFileCacheStorage.updateCachedFiles(localFilePath, -LocalFileCacheStorage.BYTES_TO_KB.apply(sizeInBytes));
                    return true;
                }
            }
        } catch (IOException e) {
            LOG.warn("Error deleting temp cached file {}", downloadingLocalFilePath, e);
        }
        return false;
    }

    private Path getDownloadingLocalDir() {
        return localFilePath.getParent();
    }

    private Path getDownloadingLocalFilePath() {
        return Paths.get(localFilePath.toString() + ".downloading");
    }
}
