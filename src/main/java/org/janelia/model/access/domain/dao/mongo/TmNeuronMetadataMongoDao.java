package org.janelia.model.access.domain.dao.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.DomainUtils;
import org.janelia.model.access.domain.dao.TmNeuronBufferDao;
import org.janelia.model.access.domain.dao.TmNeuronMetadataDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * {@link TmNeuronMetadata} Mongo DAO.
 */
public class TmNeuronMetadataMongoDao extends AbstractPermissionAwareDomainMongoDao<TmNeuronMetadata> implements TmNeuronMetadataDao {
    private static final Logger LOG = LoggerFactory.getLogger(TmNeuronMetadataMongoDao.class);

    private final DomainDAO domainDao;
    private final TmNeuronBufferDao tmNeuronBufferDao;

    @Inject
    TmNeuronMetadataMongoDao(MongoDatabase mongoDatabase, ObjectMapper objectMapper, DomainDAO domainDao, TmNeuronBufferDao tmNeuronBufferDao) {
        super(mongoDatabase, objectMapper);
        this.domainDao = domainDao;
        this.tmNeuronBufferDao = tmNeuronBufferDao;
    }

    @Override
    public TmNeuronMetadata createTmNeuronInWorkspace(String subjectKey, TmNeuronMetadata neuronMetadata, TmWorkspace workspace, InputStream neuronPoints) {
        String neuronOwnerKey;
        // Inherit from parent only if not existing already
        if (!subjectKey.equals(neuronMetadata.getOwnerKey())) {
            neuronOwnerKey = neuronMetadata.getOwnerKey();
        } else {
            neuronOwnerKey = subjectKey;
            // inherit the read/write permissions from the workspace
            neuronMetadata.setReaders(workspace.getReaders());
            neuronMetadata.setWriters(workspace.getWriters());
        }
        TmNeuronMetadata persistedNeuronMetadata;
        try {
            if (neuronMetadata.getId() == null) {
                persistedNeuronMetadata = domainDao.save(neuronOwnerKey, neuronMetadata);
            } else {
                persistedNeuronMetadata = domainDao.createWithPrepopulatedId(neuronOwnerKey, neuronMetadata);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            if (neuronPoints != null) {
                attachNeuronPoints(persistedNeuronMetadata, neuronPoints);
            }
            return persistedNeuronMetadata;
        } catch (Exception e) {
            try {
                domainDao.remove(persistedNeuronMetadata.getOwnerKey(), persistedNeuronMetadata);
            } catch (Exception ignore) {
                LOG.warn("Error removing {} after failing to attach neuron points", persistedNeuronMetadata, ignore);
            }
            throw new IllegalStateException(e);
        }
    }

    private void attachNeuronPoints(TmNeuronMetadata neuron, InputStream neuronPoints) {
        tmNeuronBufferDao.createNeuronWorkspacePoints(neuron.getId(), neuron.getWorkspaceId(), neuronPoints);
    }

    @Override
    public List<TmNeuronMetadata> getTmNeuronMetadataByWorkspaceId(String subjectKey, Long workspaceId) {
        String workspaceRef = "TmWorkspace#"+workspaceId;

        return find(MongoDaoHelper.createFilterCriteria(
                ImmutableList.of(Filters.eq("workspaceRef", workspaceRef), createSubjectReadPermissionFilter(subjectKey))),
                null, 0, -1,
                getEntityType());
    }

    @Override
    public List<Pair<TmNeuronMetadata, InputStream>> getTmNeuronsMetadataWithPointStreamsByWorkspaceId(String subjectKey, TmWorkspace workspace) {
        Map<Long, TmNeuronMetadata> workspaceNeurons = DomainUtils.getMapById(getTmNeuronMetadataByWorkspaceId(subjectKey, workspace.getId()));

        Map<Long, InputStream> neuronsPointStreams = tmNeuronBufferDao.streamNeuronPointsByWorkspaceId(Collections.emptySet(), workspace.getId());

        List<Pair<TmNeuronMetadata,InputStream>> neuronList = new ArrayList<>();

        neuronsPointStreams.forEach((neuronId, neuronPointStream) -> {
            TmNeuronMetadata neuron = workspaceNeurons.get(neuronId);
            if (neuron == null) {
                neuron = createNeuronOrUpdateNeuronWorkspace(neuronId, workspace);
            }
            neuronList.add(ImmutablePair.of(neuron, neuronPointStream));
        });

        return neuronList;
    }

    private TmNeuronMetadata createNeuronOrUpdateNeuronWorkspace(Long neuronId, TmWorkspace workspace) {
        TmNeuronMetadata neuron = findById(neuronId);
        if (neuron != null) {
            // the neuron exist but it's not associated with the specified workspace
            neuron.setWorkspaceRef(Reference.createFor(workspace));
        } else {
            Date now = new Date();
            neuron = new TmNeuronMetadata();
            neuron.setId(neuronId);
            neuron.setWorkspaceRef(Reference.createFor(workspace));
            neuron.setCreationDate(now);
            neuron.setUpdatedDate(now);
            neuron.setOwnerKey(workspace.getOwnerKey());
            neuron.setReaders(Sets.newHashSet(neuron.getOwnerKey()));
            neuron.setWriters(Sets.newHashSet(neuron.getOwnerKey()));
        }
        try {
            return saveWithSubjectKey(neuron, neuron.getOwnerKey());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void updateNeuronPoints(TmNeuronMetadata neuron, InputStream neuronPoints) {
        tmNeuronBufferDao.updateNeuronWorkspacePoints(neuron.getId(), neuron.getWorkspaceId(), neuronPoints);
    }
}
