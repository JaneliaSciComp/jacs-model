package org.janelia.model.access.domain.dao.searchables;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.DomainObjectDao;
import org.janelia.model.access.domain.dao.SyncedPathDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.files.SyncedPath;
import org.janelia.model.domain.files.SyncedRoot;

import javax.inject.Inject;
import java.util.List;

@AsyncIndex
public class SyncedPathSearchableDao extends AbstractDomainSearchableDao<SyncedPath> implements SyncedPathDao {

    private final SyncedPathDao syncedPathDao;

    @Inject
    public SyncedPathSearchableDao(SyncedPathDao syncedPathDao,
                                   DomainObjectDao<SyncedPath> domainObjectDao,
                                   @AsyncIndex DomainObjectIndexer domainObjectIndexer) {
        super(domainObjectDao, domainObjectIndexer);
        this.syncedPathDao = syncedPathDao;
    }

    @Override
    public SyncedRoot createSyncedRoot(String subjectKey, SyncedRoot syncedRoot) {
        SyncedRoot savedSyncedRoot = createSyncedRoot(subjectKey, syncedRoot);
        domainObjectIndexer.indexDocument(savedSyncedRoot);
        return savedSyncedRoot;
    }

    @Override
    public void removeSyncedRoot(String subjectKey, SyncedRoot syncedRoot) {
        removeSyncedRoot(subjectKey, syncedRoot);
        domainObjectIndexer.removeDocument(syncedRoot.getId());
    }

    @Override
    public SyncedPath addSyncedPath(String subjectKey, SyncedRoot syncedRoot, SyncedPath syncedPath) {
        return addSyncedPath(subjectKey, syncedRoot, syncedPath);
    }

    @Override
    public void removeSyncedPath(String subjectKey, SyncedRoot syncedRoot, SyncedPath syncedPath) {
        removeSyncedPath(subjectKey, syncedRoot, syncedPath);
    }

    @Override
    public List<SyncedRoot> getSyncedRoots(String subjectKey) {
        return syncedPathDao.getSyncedRoots(subjectKey);
    }

    @Override
    public List<SyncedPath> getChildren(String subjectKey, SyncedRoot root, long offset, int length) {
        return syncedPathDao.getChildren(subjectKey, root, offset, length);
    }
}
