package org.janelia.rendering;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBasedDataLocation implements DataLocation {

    private static final Logger LOG = LoggerFactory.getLogger(FileBasedDataLocation.class);

    private final Path baseDataStoragePath;
    final Function<Path, Path> pathMapper;

    public FileBasedDataLocation(Path baseDataStoragePath, Function<Path, Path> pathMapper) {
        this.baseDataStoragePath = baseDataStoragePath;
        this.pathMapper = pathMapper == null ? Function.identity() : pathMapper;
    }

    FileBasedDataLocation(FileBasedDataLocation fileBasedDataLocation) {
        this(fileBasedDataLocation.baseDataStoragePath, fileBasedDataLocation.pathMapper);
    }

    @Override
    public URI getConnectionURI() {
        // for local file based location the connection URI and the base data storage URI are the same.
        return pathMapper.apply(baseDataStoragePath).toUri();
    }

    @Override
    public URI getDataStorageURI() {
        return pathMapper.apply(baseDataStoragePath).toUri();
    }

    @Override
    public String getBaseDataStoragePath() {
        return StringUtils.replace(pathMapper.apply(baseDataStoragePath).toString(), "\\", "/");
    }

    @Override
    public String getContentURIFromRelativePath(String relativePath) {
        return getLocationPathFromRelativePath(relativePath).toString();
   }

    private Path getLocationPathFromRelativePath(String relativePath) {
        return pathMapper.apply(baseDataStoragePath).resolve(relativePath);
    }

    @Override
    public String getContentURIFromAbsolutePath(String absolutePath) {
        return getLocationPathFromAbsolutePath(absolutePath).toString();
    }

    private Path getLocationPathFromAbsolutePath(String absolutePath) {
        return pathMapper.apply(Paths.get(absolutePath));
    }

    @Override
    public Streamable<InputStream> getContentFromRelativePath(String relativePath, StorageOptions storageOptions) {
        return openContentStream(getLocationPathFromRelativePath(relativePath), defaultPathHandler());
    }

    @Override
    public Streamable<InputStream> getContentFromAbsolutePath(String absolutePath, StorageOptions storageOptions) {
        Preconditions.checkArgument(StringUtils.isNotBlank(absolutePath));
        return openContentStream(getLocationPathFromAbsolutePath(absolutePath), defaultPathHandler());
    }

    Function<Path, InputStream> defaultPathHandler() {
        return (Path p) -> {
            try {
                return Files.newInputStream(p);
            } catch (IOException e) {
                LOG.error("Error opening {}", p, e);
                throw new IllegalStateException(e);
            }
        };
    }

    private Streamable<InputStream> openContentStream(Path fp, Function<Path, InputStream> contentStreamSupplier) {
        if (Files.exists(fp)) {
            try {
                return Streamable.of(contentStreamSupplier.apply(fp), Files.size(fp));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            return Streamable.empty();
        }
    }

    @Override
    public boolean checkContentAtRelativePath(String relativePath, StorageOptions storageOptions) {
        return Files.exists(getLocationPathFromRelativePath(relativePath));
    }

    @Override
    public boolean checkContentAtAbsolutePath(String absolutePath, StorageOptions storageOptions) {
        Preconditions.checkArgument(StringUtils.isNotBlank(absolutePath));
        return Files.exists(getLocationPathFromAbsolutePath(absolutePath));
    }
}
