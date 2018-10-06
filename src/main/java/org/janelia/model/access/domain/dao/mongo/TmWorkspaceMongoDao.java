package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.dao.TmWorkspaceDao;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

import javax.inject.Inject;
import java.util.List;

/**
 * TmWorkspace Mongo DAO.
 */
public class TmWorkspaceMongoDao extends AbstractPermissionAwareDomainMongoDao<TmWorkspace> implements TmWorkspaceDao {
    private final DomainDAO domainDao;

    @Inject
    TmWorkspaceMongoDao(MongoDatabase mongoDatabase, DomainDAO domainDao) {
        super(mongoDatabase);
        this.domainDao = domainDao;
    }

    @Override
    public List<TmWorkspace> getTmWorkspacesForSample(String subjectKey, Long sampleId) {
        return domainDao.getDomainObjectsWithProperty(subjectKey, TmWorkspace.class, "sampleRef", "TmSample#"+sampleId);
    }

}
