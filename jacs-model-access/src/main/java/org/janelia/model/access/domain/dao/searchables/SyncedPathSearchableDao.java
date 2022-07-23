package org.janelia.model.access.domain.dao.searchables;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.DomainObjectDao;
import org.janelia.model.access.domain.dao.SyncedRootDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.files.SyncedRoot;

import javax.inject.Inject;
import java.util.List;

@AsyncIndex
public class SyncedPathSearchableDao extends AbstractDomainSearchableDao<SyncedRoot> implements SyncedRootDao {

    private final SyncedRootDao syncedPathDao;

    @Inject
    public SyncedPathSearchableDao(SyncedRootDao syncedPathDao,
                                   DomainObjectDao<SyncedRoot> domainObjectDao,
                                   @AsyncIndex DomainObjectIndexer domainObjectIndexer) {
        super(domainObjectDao, domainObjectIndexer);
        this.syncedPathDao = syncedPathDao;
    }

    @Override
    public SyncedRoot createSyncedRoot(String subjectKey, SyncedRoot syncedRoot) {
        SyncedRoot savedSyncedRoot = syncedPathDao.createSyncedRoot(subjectKey, syncedRoot);
        domainObjectIndexer.indexDocument(savedSyncedRoot);
        return savedSyncedRoot;
    }

    @Override
    public void removeSyncedRoot(String subjectKey, SyncedRoot syncedRoot) {
        syncedPathDao.removeSyncedRoot(subjectKey, syncedRoot);
        domainObjectIndexer.removeDocument(syncedRoot.getId());
    }

    @Override
    public List<SyncedRoot> getSyncedRoots(String subjectKey) {
        return syncedPathDao.getSyncedRoots(subjectKey);
    }

    @Override
    public SyncedRoot updateChildren(String subjectKey, SyncedRoot syncedRoot, List<Reference> newChildren) {
        SyncedRoot updated = updateChildren(subjectKey, syncedRoot, newChildren);
        domainObjectIndexer.indexDocument(updated);
        return updated;
    }
}
