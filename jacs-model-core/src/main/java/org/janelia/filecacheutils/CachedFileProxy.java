package org.janelia.filecacheutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cached file representation - implements a proxy for accessing content from a locally cached file.
 * @param <K> delegate key type
 */
public class CachedFileProxy<K extends FileKey> implements FileProxy {
    private static final Logger LOG = LoggerFactory.getLogger(CachedFileProxy.class);
    private static final Set<String> DOWNLOADING_FILES = new ConcurrentSkipListSet<>();
    private static final int DEFAULT_RETRIES = 3;

    private final Path localFilePath;
    private final K delegateFileKey;
    private final FileKeyToProxyMapper<K> delegateFileKeyToProxyMapper;
    private final LocalFileCacheStorage localFileCacheStorage;
    private final ExecutorService localFileWriterExecutor;
    private FileProxy delegateFileProxy;

    CachedFileProxy(K delegateFileKey, FileKeyToProxyMapper<K> delegateFileKeyToProxyMapper, LocalFileCacheStorage localFileCacheStorage, ExecutorService localFileWriterExecutor) {
        this.delegateFileKey = delegateFileKey;
        this.localFileCacheStorage = localFileCacheStorage;
        this.localFilePath = delegateFileKey.getLocalPath(localFileCacheStorage);
        Preconditions.checkArgument(localFilePath != null, "Local path cannot be retrieved from fileKey " + delegateFileKey);
        this.delegateFileKeyToProxyMapper = delegateFileKeyToProxyMapper;
        this.localFileWriterExecutor = localFileWriterExecutor;
        this.delegateFileProxy = null;
    }

    @Override
    public String getFileId() {
        return localFilePath.toString();
    }

    private FileProxy getFileProxy() throws FileNotFoundException {
        if (delegateFileProxy == null) {
            delegateFileProxy = delegateFileKeyToProxyMapper.getProxyFromKey(delegateFileKey);
        }
        return delegateFileProxy;
    }

    @Override
    public Long estimateSizeInBytes(boolean alwaysCheck) {
        long currentSize = alwaysCheck ? 0L : getFileSize(localFilePath);
        if (currentSize > 0) {
            return currentSize;
        } else {
            try {
                FileProxy fp = getFileProxy();
                return fp != null ? fp.estimateSizeInBytes(alwaysCheck) : 0L;
            } catch (FileNotFoundException e) {
                return 0L;
            }
        }
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
    public InputStream openContentStream(boolean alwaysDownload) throws FileNotFoundException {
        InputStream localFileStream = alwaysDownload ? null : localFileCacheStorage.openLocalCachedFile(localFilePath);
        if (localFileStream != null) {
            return localFileStream;
        } else {
            FileProxy fp = getFileProxy();
            if (fp == null) {
                return null;
            }
            ContentStream contentStream = new RetriedContentStream(() -> fp.openContentStream(alwaysDownload), DEFAULT_RETRIES);
            Long estimatedSize = fp.estimateSizeInBytes(alwaysDownload);

            if (alwaysDownload || !localFileCacheStorage.isBytesSizeAcceptable(estimatedSize) || !DOWNLOADING_FILES.add(localFilePath.toString())) {
                // if this file is either null or too large and caching will exceed the cache capacity
                // or some other thread is already downloading this file
                // simply return the stream directly and do not attempt any caching
                return contentStream;
            }
            try {
                // prepare what's need it to cache the file locally
                makeDownloadDir();
                Path downloadingLocalFilePath = getDownloadingLocalFilePath();
                OutputStream downloadingLocalFileStream = new FileOutputStream(downloadingLocalFilePath.toFile());
                // tee the remote stream to a local file image which is being written as the remote stream is being read.
                return new TeeInputStream(contentStream,
                        downloadingLocalFileStream,
                        (Void) -> {
                            try {
                                downloadingLocalFileStream.close();
                                persistToLocalCache(downloadingLocalFilePath, estimatedSize);
                            } catch (Exception e) {
                                // Instead of ignoring this, it's a warning because on Windows file cannot be moved if it is in use
                                LOG.warn("Error closing downloading local file {}", downloadingLocalFilePath, e);
                            }
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
                                persistToLocalCache(downloadingLocalFilePath, estimatedSize);
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
            } catch (Exception e) {
                LOG.warn("Error opening temporary download file", e);
                return contentStream; // in case of any exception return the content stream and don't attempt any caching
            }
        }
    }

    /**
     * atomically rename downloaded file.
     * @param downloadedFilePath location of the temporary file created during download
     */
    private synchronized void persistToLocalCache(Path downloadedFilePath, Long expectedSize) {
        try {
            LOG.debug("Move {} -> {}", downloadedFilePath, localFilePath);
            Path localDir = localFilePath.getParent();
            if (localDir != null && Files.notExists(localDir)) {
                Files.createDirectories(localDir);
            }
            if (Files.exists(downloadedFilePath)) {
                if (expectedSize == null || expectedSize <= 0 || expectedSize == Files.size(downloadedFilePath)) {
                    Files.move(downloadedFilePath, localFilePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                    // update the cache size
                    localFileCacheStorage.updateCachedFiles(localFilePath, LocalFileCacheStorage.BYTES_TO_KB.apply(getFileSize(localFilePath)));
                } else {
                    LOG.warn("Skip caching {} because its size ({}) differs from the expected size ({})", localFilePath, getFileSize(downloadedFilePath), expectedSize);
                }
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
            }
        }
    }

    @Override
    public File getLocalFile(boolean alwaysDownload) throws FileNotFoundException {
        Path localCachedFilePath = localFileCacheStorage.getLocalCachedFile(localFilePath);
        if (!alwaysDownload && localCachedFilePath != null) {
            return localCachedFilePath.toFile();
        } else {
            FileProxy fp = getFileProxy();
            if (fp == null) {
                return null;
            }
            if (!DOWNLOADING_FILES.add(localFilePath.toString())) {
                // some other thread may also be downloading this file so in this case simply return the file
                // without trying to cache it locally again.
                // this is a bit risky because the download may fail but this entire method should be scrapped
                // and the calls to it should be refactored to use FileProxy instead of File
                return localFilePath.toFile();
            }
            Long estimatedSize = fp.estimateSizeInBytes(alwaysDownload);
            // download remote file
            try (ContentStream contentStream = new RetriedContentStream(() -> fp.openContentStream(alwaysDownload) , DEFAULT_RETRIES)) {
                // copy remote content to a local file
                Path downloadingLocalFilePath = getDownloadingLocalFilePath();
                makeDownloadDir();
                try (OutputStream os = Files.newOutputStream(downloadingLocalFilePath, StandardOpenOption.CREATE)) {
                    ByteStreams.copy(contentStream, os);
                    persistToLocalCache(downloadingLocalFilePath, estimatedSize);
                } catch (IOException e) {
                    LOG.error("Error saving downloadable stream to {} while downloading {}", downloadingLocalFilePath, localFilePath, e);
                    FileNotFoundException nfe = new FileNotFoundException("Error saving temp file for " + localFilePath);
                    nfe.initCause(e);
                    throw nfe;
                } finally {
                    try {
                        Files.deleteIfExists(downloadingLocalFilePath);
                    } catch (Exception deleteExc) {
                        LOG.debug("Error deleting {}", downloadingLocalFilePath, deleteExc);
                    }
                }
            } catch (FileNotFoundException | IllegalStateException e) {
                // remove file from the cache in case of an error
                localFileCacheStorage.updateCachedFiles(localFilePath, -LocalFileCacheStorage.BYTES_TO_KB.apply(getFileSize(localFilePath)));
                throw e;
            } finally {
                DOWNLOADING_FILES.remove(localFilePath.toString());
            }
            return localFilePath.toFile();
        }
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
    public boolean exists(boolean alwaysCheck) {
        if (!alwaysCheck && Files.exists(localFilePath)) {
            return true;
        } else {
            try {
                FileProxy fp = getFileProxy();
                return fp != null && fp.exists(alwaysCheck);
            } catch (FileNotFoundException e) {
                return false;
            }
        }
    }

    @Override
    public boolean deleteProxy() {
        long sizeInBytes = getFileSize(localFilePath);
        try {
            if (Files.exists(localFilePath)) {
                // For safety I do not delete any file that somehow is outside the cache storage directory
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
