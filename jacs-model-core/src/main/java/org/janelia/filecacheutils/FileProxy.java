package org.janelia.filecacheutils;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

import javax.annotation.Nullable;

public interface FileProxy {
    String getFileId();

    Optional<Long> estimateSizeInBytes();

    @Nullable InputStream getContentStream();

    File getLocalFile();

    boolean deleteProxy();
}
