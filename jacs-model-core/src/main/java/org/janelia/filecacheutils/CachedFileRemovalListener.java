package org.janelia.filecacheutils;

import java.util.Optional;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class CachedFileRemovalListener implements RemovalListener<FileKey, Optional<FileProxy>> {

    @Override
    public void onRemoval(RemovalNotification<FileKey, Optional<FileProxy>> notification) {
        if (notification.wasEvicted()) {
            notification.getValue().ifPresent(fp -> fp.deleteProxy());
        }
    }
}
