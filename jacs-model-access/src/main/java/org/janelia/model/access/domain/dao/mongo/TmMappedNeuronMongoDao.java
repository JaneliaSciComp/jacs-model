package org.janelia.model.access.domain.dao.mongo;

import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.janelia.model.access.domain.IdGenerator;
import org.janelia.model.access.domain.dao.TmMappedNeuronDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.tiledMicroscope.TmMappedNeuron;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

@Dependent
public class TmMappedNeuronMongoDao extends AbstractDomainObjectMongoDao<TmMappedNeuron> implements TmMappedNeuronDao {

    @Inject
    TmMappedNeuronMongoDao(MongoDatabase mongoDatabase,
                           IdGenerator<Long> idGenerator,
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
        // TODO: this should remove the deleted documents from the search index
        return MongoDaoHelper.deleteMatchingRecords(mongoCollection,
                Filters.and(MongoDaoHelper.createFilterCriteria(
                        Filters.eq("workspaceRef", Reference.createFor(workspace))
                ), permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey)));
    }
}
