package org.janelia.model.access.domain.dao.searchables;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.TmWorkspaceDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

import javax.inject.Inject;
import java.util.List;

/**
 * {@link TmWorkspace} DAO.
 */
@AsyncIndex
public class TmWorkspaceSearchableDao extends AbstractDomainSearchablDao<TmWorkspace> implements TmWorkspaceDao {

    private final TmWorkspaceDao tmWorkspaceDao;

    @Inject
    TmWorkspaceSearchableDao(TmWorkspaceDao tmWorkspaceDao,
                             @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(tmWorkspaceDao, objectIndexer);
        this.tmWorkspaceDao = tmWorkspaceDao;
    }

    @Override
    public List<TmWorkspace> getTmWorkspacesForSample(String subjectKey, Long sampleId) {
        return tmWorkspaceDao.getTmWorkspacesForSample(subjectKey, sampleId);
    }

    @Override
    public TmWorkspace createTmWorkspace(String subjectKey, TmWorkspace tmWorkspace) {
        TmWorkspace persistedTmWorkspace = tmWorkspaceDao.createTmWorkspace(subjectKey, tmWorkspace);
        domainObjectIndexer.indexDocument(persistedTmWorkspace);
        return persistedTmWorkspace;
    }

    @Override
    public TmWorkspace copyTmWorkspace(String subjectKey, TmWorkspace existingWorkspace, String newName, String assignOwner) {
        TmWorkspace copiedTmWorkspace = tmWorkspaceDao.copyTmWorkspace(subjectKey, existingWorkspace, newName, assignOwner);
        domainObjectIndexer.indexDocument(copiedTmWorkspace);
        return copiedTmWorkspace;
    }

    @Override
    public TmWorkspace updateTmWorkspace(String subjectKey, TmWorkspace tmWorkspace) {
        TmWorkspace updatedTmWorkspace = tmWorkspaceDao.updateTmWorkspace(subjectKey, tmWorkspace);
        domainObjectIndexer.indexDocument(updatedTmWorkspace);
        return updatedTmWorkspace;
    }
}
