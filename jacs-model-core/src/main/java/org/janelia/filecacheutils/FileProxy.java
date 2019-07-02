package org.janelia.filecacheutils;

import java.io.File;
import java.io.InputStream;

import javax.annotation.Nullable;

public interface FileProxy {
    String getFileId();
    @Nullable Long getSizeInBytes();
    InputStream getContentStream();
    File getLocalFile();
    boolean delete();
}
