package org.janelia.filecacheutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Proxy for accessing content from locally accessible files.
 */
public class LocalFileProxy implements FileProxy {

    private final Path localFilePath;

    public LocalFileProxy(Path localFilePath) {
        Preconditions.checkArgument(localFilePath != null);
        this.localFilePath = localFilePath;
    }

    public LocalFileProxy(String localFilePath) {
        this.localFilePath = Paths.get(localFilePath);
    }

    @Override
    public String getFileId() {
        return localFilePath.toString();
    }

    @Override
    public Long estimateSizeInBytes() {
        try {
            if (Files.exists(localFilePath)) {
                return Files.size(localFilePath);
            } else {
                return 0L;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public InputStream openContentStream() throws FileNotFoundException {
        if (Files.notExists(localFilePath)) {
            throw new FileNotFoundException(localFilePath + " was not found");
        }
        return new ContentStream(() -> {
            try {
                return new FileInputStream(localFilePath.toFile());
            } catch (IOException e) {
                throw new IllegalStateException("Error opening " + localFilePath, e);
            }
        });
    }

    @Override
    public File getLocalFile() throws FileNotFoundException{
        if (Files.exists(localFilePath)) {
            return localFilePath.toFile();
        } else {
            throw new FileNotFoundException("Path " + localFilePath + " does not exist");
        }
    }

    @Override
    public boolean exists() {
        return Files.exists(localFilePath);
    }

    @Override
    public boolean deleteProxy() {
        return false;
    }

}
