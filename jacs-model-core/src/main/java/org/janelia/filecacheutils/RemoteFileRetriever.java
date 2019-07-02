package org.janelia.filecacheutils;

public interface RemoteFileRetriever<K extends FileKey> {
    FileProxy retrieve(K fileKey);
}
