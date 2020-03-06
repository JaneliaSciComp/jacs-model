package org.janelia.filecacheutils;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import com.google.common.cache.CacheLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local file cache loader.
 *
 * @param <K> cache entry key
 */
public class RemoteFileLoader<K extends FileKey> extends CacheLoader<K, FileProxy> {

    private final LocalFileCacheStorage localFileCacheStorage;
    private final FileKeyToProxyMapper<K> fileKeyToProxyMapper;
    private final ExecutorService localFileWriterExecutor;

    RemoteFileLoader(LocalFileCacheStorage localFileCacheStorage, FileKeyToProxyMapper<K> fileKeyToProxyMapper, ExecutorService localFileWriterExecutor) {
        this.localFileCacheStorage = localFileCacheStorage;
        this.fileKeyToProxyMapper = fileKeyToProxyMapper;
        this.localFileWriterExecutor = localFileWriterExecutor;
    }

    @Override
    public FileProxy load(K fileKey) {
        return new CachedFileProxy<>(fileKey, fileKeyToProxyMapper, localFileCacheStorage, localFileWriterExecutor);
    }
}
