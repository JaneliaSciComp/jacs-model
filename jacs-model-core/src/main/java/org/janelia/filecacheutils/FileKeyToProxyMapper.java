package org.janelia.filecacheutils;

import java.io.FileNotFoundException;

public interface FileKeyToProxyMapper<K extends FileKey> {
    FileProxy getProxyFromKey(K fileKey) throws FileNotFoundException;
}
