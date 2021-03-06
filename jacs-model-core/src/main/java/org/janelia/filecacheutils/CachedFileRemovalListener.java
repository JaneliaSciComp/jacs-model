package org.janelia.filecacheutils;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class CachedFileRemovalListener implements RemovalListener<FileKey, FileProxy> {
    @Override
    public void onRemoval(RemovalNotification<FileKey, FileProxy> notification) {
        if (notification.wasEvicted()) {
            notification.getValue().deleteProxy();
        }
    }
}
