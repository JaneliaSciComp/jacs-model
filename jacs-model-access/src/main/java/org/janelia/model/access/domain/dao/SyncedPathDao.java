package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.files.SyncedPath;
import org.janelia.model.domain.files.SyncedRoot;

import java.util.List;

public interface SyncedPathDao extends DomainObjectDao<SyncedPath> {

    /**
     * Create the given SyncedRoot. The id should be null.
     * @param subjectKey subject who will own the SyncedRoot
     * @param syncedRoot the SyncedRoot attributes to create
     * @return saved SyncedRoot with id filled in
     */
    SyncedRoot createSyncedRoot(String subjectKey, SyncedRoot syncedRoot);

    /**
     * Remove the given SyncedRoot and all of its SyncedPath children.
     * @param subjectKey subject with write access to the SyncedRoot
     * @param syncedRoot the SyncedRoot to delete
     */
    void removeSyncedRoot(String subjectKey, SyncedRoot syncedRoot);

    SyncedPath addSyncedPath(String subjectKey, SyncedRoot syncedRoot, SyncedPath syncedPath);

    void removeSyncedPath(String subjectKey, SyncedRoot syncedRoot, SyncedPath syncedPath);

    /**
     * Returns all of the SyncedRoot objects that a user has access to read.
     * @param subjectKey the user or group
     * @return list of top-level synced paths
     */
    List<SyncedRoot> getSyncedRoots(String subjectKey);

    /**
     * Returns all the children SyncedPath which have the given SyncedRoot as their parent.
     * @param root the root path
     * @param offset index of first item to return
     * @param length number of results to return
     * @return children of the given root
     */
    List<SyncedPath> getChildren(String subjectKey, SyncedRoot root, long offset, int length);

}
