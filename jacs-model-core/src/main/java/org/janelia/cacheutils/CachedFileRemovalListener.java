package org.janelia.cacheutils;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class CachedFileRemovalListener implements RemovalListener<CachedFileKey, CachedFile> {

    @Override
    public void onRemoval(RemovalNotification<CachedFileKey, CachedFile> notification) {
        if (notification.getValue() != null && notification.wasEvicted()) {
            notification.getValue().delete();
        }
    }
}
