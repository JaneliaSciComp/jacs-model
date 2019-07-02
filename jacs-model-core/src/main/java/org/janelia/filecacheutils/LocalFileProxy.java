package org.janelia.filecacheutils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nullable;

public class LocalFileProxy implements FileProxy {

    private final Path localFilePath;

    public LocalFileProxy(String localFilePath) {
        this.localFilePath = Paths.get(localFilePath);
    }

    @Override
    public String getFileId() {
        return localFilePath.toString();
    }

    @Nullable
    @Override
    public Long getSizeInBytes() {
        try {
            if (Files.exists(localFilePath)) {
                return Files.size(localFilePath);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public InputStream getContentStream() {
        try {
            return Files.newInputStream(localFilePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public File getLocalFile() {
        return localFilePath.toFile();
    }

    @Override
    public boolean delete() {
        try {
            return Files.deleteIfExists(localFilePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
