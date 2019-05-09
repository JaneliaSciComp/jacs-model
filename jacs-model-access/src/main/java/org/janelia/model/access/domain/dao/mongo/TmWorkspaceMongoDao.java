package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.dao.TmNeuronMetadataDao;
import org.janelia.model.access.domain.dao.TmWorkspaceDao;
import org.janelia.model.domain.DomainConstants;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.tiledMicroscope.TmGeoAnnotation;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmProtobufExchanger;
import org.janelia.model.domain.tiledMicroscope.TmSample;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;
import org.janelia.model.domain.workspace.TreeNode;
import org.janelia.model.access.domain.IdSource;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link TmWorkspace} Mongo DAO.
 */
public class TmWorkspaceMongoDao extends AbstractDomainObjectMongoDao<TmWorkspace> implements TmWorkspaceDao {

    private final DomainDAO domainDao;
    private final TmNeuronMetadataDao tmNeuronMetadataDao;

    @Inject
    TmWorkspaceMongoDao(MongoDatabase mongoDatabase,
                        DomainPermissionsMongoHelper permissionsHelper,
                        DomainUpdateMongoHelper updateHelper,
                        DomainDAO domainDao,
                        TmNeuronMetadataDao tmNeuronMetadataDao) {
        super(mongoDatabase, permissionsHelper, updateHelper);
        this.domainDao = domainDao;
        this.tmNeuronMetadataDao = tmNeuronMetadataDao;
    }

    @Override
    public List<TmWorkspace> getTmWorkspacesForSample(String subjectKey, Long sampleId) {
        if (StringUtils.isBlank(subjectKey))
            return Collections.emptyList();
        return MongoDaoHelper.find(
                MongoDaoHelper.createFilterCriteria(
                        Filters.eq("sampleRef", Reference.createFor(TmSample.class, sampleId).toString()),
                        permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey)),
                null,
                0,
                -1,
                mongoCollection,
                TmWorkspace.class);
    }

    @Override
    public TmWorkspace createTmWorkspace(String subjectKey, TmWorkspace tmWorkspace) {
        try {
            TmWorkspace workspace = domainDao.save(subjectKey, tmWorkspace);
            TreeNode folder = domainDao.getOrCreateDefaultFolder(subjectKey, DomainConstants.NAME_TM_WORKSPACE_FOLDER);
            domainDao.addChildren(subjectKey, folder, Arrays.asList(Reference.createFor(workspace)));
            return workspace;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public TmWorkspace copyTmWorkspace(String subjectKey, TmWorkspace existingWorkspace, String newName, String assignOwner) {
        // Create a copy of the workspace object with the new name
        TmWorkspace workspaceCopy = createTmWorkspace(subjectKey, TmWorkspace.copy(existingWorkspace).rename(newName));
        try {
            // Copy the neurons
            List<Pair<TmNeuronMetadata, InputStream>> tmNeuronsPairs = tmNeuronMetadataDao.getTmNeuronsMetadataWithPointStreamsByWorkspaceId(subjectKey, existingWorkspace);

            // Pre-generate the neuron ids, because we need them as parent ids inside the TmNeuronData
            IdSource neuronIdSource = new IdSource(tmNeuronsPairs.size());

            for (Pair<TmNeuronMetadata, InputStream> pair : tmNeuronsPairs) {
                TmNeuronMetadata neuronCopy = TmNeuronMetadata.copy(pair.getKey());
                neuronCopy.setId(neuronIdSource.next());
                neuronCopy.setWorkspaceRef(Reference.createFor(workspaceCopy));
                if (assignOwner != null) {
                    neuronCopy.setOwnerKey(assignOwner);
                }
                InputStream oldNeuronStream = pair.getValue();

                TmProtobufExchanger protobufExchanger = new TmProtobufExchanger();
                protobufExchanger.deserializeNeuron(oldNeuronStream, neuronCopy);

                // Change the parent of the roots to be the neuron id
                for (TmGeoAnnotation annotation : neuronCopy.getRootAnnotations()) {
                    annotation.setParentId(neuronCopy.getId());
                }

                InputStream newNeuronStream = new ByteArrayInputStream(protobufExchanger.serializeNeuron(neuronCopy));
                tmNeuronMetadataDao.createTmNeuronInWorkspace(subjectKey, neuronCopy, workspaceCopy, newNeuronStream);
            }
        } catch (Exception e) {
            deleteByIdAndSubjectKey(workspaceCopy.getId(), subjectKey);
            throw new IllegalStateException(e);
        }
        return workspaceCopy;
    }

    @Override
    public TmWorkspace updateTmWorkspace(String subjectKey, TmWorkspace tmWorkspace) {
        try {
            return domainDao.save(subjectKey, tmWorkspace);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
