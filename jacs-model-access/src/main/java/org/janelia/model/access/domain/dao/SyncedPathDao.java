package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.files.SyncedPath;
import org.janelia.model.domain.files.SyncedRoot;

import java.util.List;

public interface SyncedPathDao extends DomainObjectDao<SyncedPath> {

    /**
     * Returns all the children SyncedPath which have the given SyncedRoot as their parent.
     * @param root the root path
     * @param offset index of first item to return
     * @param length number of results to return
     * @return children of the given root
     */
    List<SyncedPath> getChildren(SyncedRoot root, long offset, int length);

}
