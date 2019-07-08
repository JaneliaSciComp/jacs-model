package org.janelia.filecacheutils;

import java.util.function.Supplier;

public interface FileKeyToProxySupplier<K extends FileKey> {
    Supplier<FileProxy> getProxyFromKey(K fileKey);
}
