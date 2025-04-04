package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.TmMappedNeuronDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.tiledMicroscope.TmMappedNeuron;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

public class TmMappedNeuronMongoDao extends AbstractDomainObjectMongoDao<TmMappedNeuron> implements TmMappedNeuronDao {
    private static final Logger LOG = LoggerFactory.getLogger(TmMappedNeuronMongoDao.class);


    @Inject
    TmMappedNeuronMongoDao(MongoDatabase mongoDatabase,
                             TimebasedIdentifierGenerator idGenerator,
                             DomainPermissionsMongoHelper permissionsHelper,
                             DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public List<TmMappedNeuron> getNeuronsForWorkspace(TmWorkspace workspace) {
        return find(
                MongoDaoHelper.createFilterCriteria(
                        Filters.eq("workspaceRef", Reference.createFor(workspace))
                ),
                null,
                0,
                1,
                TmMappedNeuron.class
        );
    }

    @Override
    public long deleteNeuronsForWorkspace(TmWorkspace workspace, String subjectKey) {

        LOG.info("Deleting neurons from workspace {} in mongo collection {}",
                workspace.getName(), workspace.getNeuronCollection());
        return MongoDaoHelper.deleteMatchingRecords(mongoCollection,
                Filters.and(MongoDaoHelper.createFilterCriteria(
                        Filters.eq("workspaceRef", Reference.createFor(workspace))
                ), permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey)));
    }
}
