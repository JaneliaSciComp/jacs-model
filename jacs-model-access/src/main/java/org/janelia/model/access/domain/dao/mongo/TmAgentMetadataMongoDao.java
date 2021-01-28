package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.TmAgentDao;
import org.janelia.model.access.domain.dao.TmReviewTaskDao;
import org.janelia.model.domain.tiledMicroscope.TmAgentMetadata;
import org.janelia.model.domain.tiledMicroscope.TmGeoAnnotation;
import org.janelia.model.domain.tiledMicroscope.TmReviewTask;

import javax.inject.Inject;
import java.util.List;

/**
 * {@link TmReviewTask} Mongo DAO.
 */
public class TmAgentMetadataMongoDao extends AbstractDomainObjectMongoDao<TmAgentMetadata> implements TmAgentDao {

    private final DomainDAO domainDao;

    @Inject
    TmAgentMetadataMongoDao(MongoDatabase mongoDatabase,
                            TimebasedIdentifierGenerator idGenerator,
                            DomainPermissionsMongoHelper permissionsHelper,
                            DomainUpdateMongoHelper updateHelper,
                            DomainDAO domainDao) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
        this.domainDao = domainDao;
    }

    @Override
    public TmAgentMetadata getTmAgentMetadata(Long workspaceId, String subjectKey) {
        List<TmAgentMetadata> results = find(
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createAttributeFilter("workspaceId", workspaceId)
                ),
                null,
                0,
                -1,
                TmAgentMetadata.class);

        return results.stream().findFirst().orElse(null);
    }

    @Override
    public TmAgentMetadata createTmAgentMetadata(String subjectKey, TmAgentMetadata agentData) {
        try {
            return domainDao.save(subjectKey, agentData);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public TmAgentMetadata updateTmAgentMetadata(String subjectKey, TmAgentMetadata agentData) {
        try {
            return domainDao.save(subjectKey, agentData);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
