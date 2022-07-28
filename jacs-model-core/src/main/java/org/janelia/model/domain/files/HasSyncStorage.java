package org.janelia.model.domain.files;

import org.janelia.model.domain.interfaces.HasFilepath;

/**
 * A path which is periodically synchronized from disk to maintain a database representation.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface HasSyncStorage extends HasFilepath {

    /**
     * This flag is set to false when a desync is detected, meaning one or more of the paths in files cannot be found.
     *
     * This usually happens when someone moves files on disk without updating the database.
     *
     * @return true if the filepath existed on disk the last time we checked
     */
    boolean isExistsInStorage();

    void setExistsInStorage(boolean exists);

    /**
     * If this flag is true, the object will be synchronized to a corresponding SyncedRoot. If false, we assume
     * that the object is user managed.
     *
     * @return true if the object should be synchronized to the filesystem
     */
    boolean isAutoSynchronized();

    void setAutoSynchronized(boolean autoSynchronized);
}
