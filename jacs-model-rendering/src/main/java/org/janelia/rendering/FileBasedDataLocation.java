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

    final Path baseDataStoragePath;

    public FileBasedDataLocation(Path baseDataStoragePath) {
        this.baseDataStoragePath = baseDataStoragePath;
    }

    @Override
    public URI getConnectionURI() {
        // for local file based location the connection URI and the base data storage URI are the same.
        return baseDataStoragePath.toUri();
    }

    @Override
    public URI getDataStorageURI() {
        return baseDataStoragePath.toUri();
    }

    @Override
    public String getBaseDataStoragePath() {
        return StringUtils.replace(baseDataStoragePath.toString(), "\\", "/");
    }

    @Override
    public Streamable<InputStream> getContentFromRelativePath(String relativePath) {
        return openContentStream(baseDataStoragePath.resolve(relativePath), defaultPathHandler());
    }

    @Override
    public Streamable<InputStream> getContentFromAbsolutePath(String absolutePath) {
        Preconditions.checkArgument(StringUtils.isNotBlank(absolutePath));
        return openContentStream(Paths.get(absolutePath), defaultPathHandler());
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
    public boolean checkContentAtRelativePath(String relativePath) {
        return Files.exists(baseDataStoragePath.resolve(relativePath));
    }

    @Override
    public boolean checkContentAtAbsolutePath(String absolutePath) {
        Preconditions.checkArgument(StringUtils.isNotBlank(absolutePath));
        return Files.exists(Paths.get(absolutePath));
    }
}
