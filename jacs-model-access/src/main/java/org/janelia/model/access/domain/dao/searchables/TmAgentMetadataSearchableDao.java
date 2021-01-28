package org.janelia.model.access.domain.dao.searchables;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.TmAgentDao;
import org.janelia.model.access.domain.dao.TmReviewTaskDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.tiledMicroscope.TmAgentMetadata;
import org.janelia.model.domain.tiledMicroscope.TmReviewTask;

import javax.inject.Inject;
import java.util.List;

/**
 * {@link TmReviewTask} DAO.
 */
@AsyncIndex
public class TmAgentMetadataSearchableDao extends AbstractDomainSearchableDao<TmAgentMetadata> implements TmAgentDao {

    private final TmAgentDao tmAgentDao;

    @Inject
    TmAgentMetadataSearchableDao(TmAgentDao tmAgentDao,
                                 @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(tmAgentDao, objectIndexer);
        this.tmAgentDao = tmAgentDao;
    }

    @Override
    public TmAgentMetadata getTmAgentMetadata(Long workspaceId, String subjectKey) {
        return tmAgentDao.getTmAgentMetadata(workspaceId, subjectKey);
    }

    @Override
    public TmAgentMetadata createTmAgentMetadata(String subjectKey, TmAgentMetadata agentData) {
        TmAgentMetadata createdTmAgentMetadata = tmAgentDao.createTmAgentMetadata(subjectKey, agentData);
        return createdTmAgentMetadata;
    }

    @Override
    public TmAgentMetadata updateTmAgentMetadata(String subjectKey, TmAgentMetadata agentData) {
        TmAgentMetadata updatedTmAgentMetadata= tmAgentDao.updateTmAgentMetadata(subjectKey, agentData);
        return updatedTmAgentMetadata;
    }
}
