package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.files.SyncedRoot;

import java.util.List;

public interface SyncedRootDao extends DomainObjectDao<SyncedRoot> {

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

    /**
     * Returns all of the SyncedRoot objects that a user has access to read.
     * @param subjectKey the user or group
     * @return list of top-level synced paths
     */
    List<SyncedRoot> getSyncedRoots(String subjectKey);

    /**
     * Update the children of the given synced root, by setting a new list into the object.
     * @param subjectKey the user or group
     * @param syncedRoot the object to update in the database
     * @param newChildren new children
     * @return updated object, or null if nothing was updated
     */
    SyncedRoot updateChildren(String subjectKey, SyncedRoot syncedRoot, List<Reference> newChildren);
}
