package org.janelia.model.access.domain.dao.mongo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.dao.AppendFieldValueHandler;
import org.janelia.model.access.domain.dao.EntityFieldValueHandler;
import org.janelia.model.access.domain.dao.RemoveItemsFieldValueHandler;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.access.domain.dao.TmNeuronBufferDao;
import org.janelia.model.access.domain.dao.TmNeuronMetadataDao;
import org.janelia.model.domain.DomainUtils;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.tiledMicroscope.BulkNeuronStyleUpdate;
import org.janelia.model.domain.tiledMicroscope.TmNeuronData;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TmNeuronMetadata} Mongo DAO.
 */
public class TmNeuronMetadataMongoDao extends AbstractDomainObjectMongoDao<TmNeuronMetadata> implements TmNeuronMetadataDao {
    private static final Logger LOG = LoggerFactory.getLogger(TmNeuronMetadataMongoDao.class);

    private final DomainDAO domainDao;
    private final TmNeuronBufferDao tmNeuronBufferDao;

    @Inject
    private GridFSMongoDao gridFSMongoDao;

    @Inject
    TmNeuronMetadataMongoDao(MongoDatabase mongoDatabase,
                             DomainPermissionsMongoHelper permissionsHelper,
                             DomainUpdateMongoHelper updateHelper,
                             DomainDAO domainDao,
                             TmNeuronBufferDao tmNeuronBufferDao) {
        super(mongoDatabase, permissionsHelper, updateHelper);
        this.domainDao = domainDao;
        this.tmNeuronBufferDao = tmNeuronBufferDao;
    }

    @Override
    public TmNeuronMetadata createTmNeuronInWorkspace(String subjectKey, TmNeuronMetadata neuronMetadata, TmWorkspace workspace) {
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
            boolean isLarge = checkLargeNeuron(neuronMetadata);
            TmNeuronData pointData = neuronMetadata.getNeuronData();
            if (isLarge) {
                neuronMetadata.setNeuronData(null);
                neuronMetadata.setLargeNeuron(true);
            }
            if (neuronMetadata.getId() == null) {
                persistedNeuronMetadata = domainDao.save(neuronOwnerKey, neuronMetadata);
            } else {
                persistedNeuronMetadata = domainDao.createWithPrepopulatedId(neuronOwnerKey, neuronMetadata);
            }
            if (isLarge) {
                saveLargeNeuronPointData(persistedNeuronMetadata.getId(), pointData);
            }
            return persistedNeuronMetadata;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public TmNeuronMetadata getTmNeuronMetadata(String subjectKey, Long neuronId) {
        return findById (neuronId);
    }

    @Override
    public List<TmNeuronMetadata> getTmNeuronMetadataByWorkspaceId(String subjectKey, Long workspaceId, long offset, int length) {
        String workspaceRef = "TmWorkspace#" + workspaceId;
        List<TmNeuronMetadata> neuronList = find(
                MongoDaoHelper.createFilterCriteria(
                        Filters.eq("workspaceRef", workspaceRef),
                        permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey)),
                null,
                offset,
                length,
                getEntityType());
         hydrateLargeNeurons(neuronList);
         return neuronList;
    }

    private void saveLargeNeuronPointData (Long neuronId, TmNeuronData pointData) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            byte[] binaryData = mapper.writeValueAsBytes(pointData);
            gridFSMongoDao.updateDataBlock(new ByteArrayInputStream(binaryData),neuronId.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Problem saving large neuron to GridFS");
        }
    }

    private void removeLargeNeuronPointData (Long neuronId) {
        try {
            gridFSMongoDao.deleteDataBlock(neuronId.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Problem removing large neuron from GridFS");
        }
    }


    private boolean checkLargeNeuron (TmNeuronMetadata neuron) {
        ObjectMapper mapper = new ObjectMapper();
        int NODE_LIMIT = 40000;
        // apply quick check on node count to speed up performance
        if (neuron.getNeuronData().getGeoAnnotations().size()<NODE_LIMIT &&
                neuron.getNeuronData().getAnchoredPaths().size()<NODE_LIMIT)
            return false;
        try {
            if (mapper.writeValueAsBytes(neuron).length>10000000) {
                return true;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Problem checking the size of neuron "
                    + neuron.getId());
        }
        return false;
    }

    private void hydrateLargeNeurons(List<TmNeuronMetadata> neuronList) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            for (TmNeuronMetadata neuron: neuronList) {
                // hydrate large neurons from gridfs
                if (neuron.isLargeNeuron()) {
                    ByteArrayOutputStream outputNeuron = new ByteArrayOutputStream();
                    gridFSMongoDao.downloadDataBlock(outputNeuron, neuron.getId().toString());
                    TmNeuronData pointData = mapper.readValue(outputNeuron.toByteArray(), TmNeuronData.class);
                    neuron.setNeuronData(pointData);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
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
            return saveBySubjectKey(neuron, neuron.getOwnerKey());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean removeTmNeuron(Long neuronId, boolean isLarge, String subjectKey) {
        if (isLarge) {
            gridFSMongoDao.deleteDataBlock(neuronId.toString());
        }
        long nDeleted = deleteByIdAndSubjectKey(neuronId, subjectKey);
        if (nDeleted > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public TmNeuronMetadata saveBySubjectKey(TmNeuronMetadata neuron, String subjectKey) {
        boolean isLarge = checkLargeNeuron(neuron);
        TmNeuronData pointData = neuron.getNeuronData();
        if (isLarge) {
            neuron.setNeuronData(null);
            saveLargeNeuronPointData(neuron.getId(), pointData);
            if (!neuron.isLargeNeuron())
                super.delete(neuron);
            neuron.setLargeNeuron(isLarge);
            super.insertNewEntity(neuron);
        } else {
            if (neuron.isLargeNeuron())
                removeLargeNeuronPointData(neuron.getId());
            neuron.setLargeNeuron(isLarge);
            super.saveBySubjectKey(neuron, subjectKey);
        }
        if (isLarge)
            neuron.setNeuronData(pointData);
        return neuron;
    }

    @Override
    public void updateNeuronStyles(BulkNeuronStyleUpdate bulkNeuronStyleUpdate, String subjectKey) {
        ImmutableMap.Builder<String, EntityFieldValueHandler<?>> updatesBuilder = ImmutableMap.builder();
        if (bulkNeuronStyleUpdate.getVisible() != null) {
            updatesBuilder.put("visible", new SetFieldValueHandler<>(bulkNeuronStyleUpdate.getVisible()));
        }
        if (StringUtils.isNotBlank(bulkNeuronStyleUpdate.getColorHex())) {
            updatesBuilder.put("colorHex", new SetFieldValueHandler<>(bulkNeuronStyleUpdate.getColorHex()));
        }
        updatesBuilder.put("updatedDate", new SetFieldValueHandler<>(new Date()));
        Map<String, EntityFieldValueHandler<?>> updates = updatesBuilder.build();
        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(false);
        MongoDaoHelper.updateMany(
                mongoCollection,
                MongoDaoHelper.createFilterCriteria(
                        ImmutableList.of(
                                MongoDaoHelper.createFilterByIds(bulkNeuronStyleUpdate.getNeuronIds()),
                                permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey))
                ),
                updates,
                updateOptions);
    }

    @Override
    public void removeEmptyNeuronsInWorkspace(Long workspaceId, String subjectKey) {
        MongoDaoHelper.deleteMatchingRecords(
                mongoCollection,
                MongoDaoHelper.createFilterCriteria(
                        ImmutableList.of(
                                Filters.eq("workspaceRef", "TmWorkspace#" + workspaceId),
                                Filters.exists("neuronData", false),
                                permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey))
                ));
    }

    @Override
    public void bulkReplaceNeuronsInWorkspace(Long workspaceId, Collection<TmNeuronMetadata> neurons, String subjectKey) {
        if (neurons==null || neurons.isEmpty())
            return;
        MongoCollection<TmNeuronMetadata> copyLoc = mongoDatabase.getCollection("TmNeuronMerged", TmNeuronMetadata.class);
        List<TmNeuronMetadata> neuronList = new ArrayList<>(neurons);
        try {
            copyLoc.insertMany(neuronList);
        } catch (org.bson.BsonMaximumSizeExceededException e) {
            // one or more of the documents is too big
            // save them to gridfs and then try again
            int count = 0;
            List<Long> largeNeurons = new ArrayList<>();
            List<TmNeuronMetadata> truncatedList = new ArrayList<>();
            boolean hitFirstLarge = false;
            for (TmNeuronMetadata neuron: neuronList) {
                if (hitFirstLarge)
                    truncatedList.add(neuron);
                // remove records that were saved before batch failed
                MongoDaoHelper.deleteMatchingRecords(copyLoc,
                        Filters.and(MongoDaoHelper.createFilterById(neuron.getId()),
                                permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey)));
                boolean isLarge = checkLargeNeuron(neuron);
                TmNeuronData pointData = neuron.getNeuronData();
                if (isLarge) {
                    hitFirstLarge = true;
                    largeNeurons.add(neuron.getId());
                    neuron.setNeuronData(null);
                    saveLargeNeuronPointData(neuron.getId(), pointData);
                    neuron.setLargeNeuron(isLarge);
                }
            }
            try {
                LOG.info("Large neurons in this batch are {}",largeNeurons);
                if (!truncatedList.isEmpty())
                    copyLoc.insertMany(truncatedList);
            } catch (org.bson.BsonMaximumSizeExceededException ee) {
                ee.printStackTrace();
                LOG.info("ERROR PROCESSING {} - Issue with Large Neurons", workspaceId);
            }
        }
    }

    @Override
    public void insertTmNeurons(Collection<TmNeuronMetadata> neurons) {
        this.saveAll(neurons);
    }

    @Override
    public List<Pair<TmNeuronMetadata, InputStream>> getTmNeuronsMetadataWithPointStreamsByWorkspaceId(String subjectKey, TmWorkspace workspace, long offset, int length) {
        List<TmNeuronMetadata> workspaceNeurons = getTmNeuronMetadataByWorkspaceId(subjectKey, workspace.getId(), offset, length);
        if (workspaceNeurons.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, TmNeuronMetadata> indexedWorkspaceNeurons = DomainUtils.getMapById(workspaceNeurons);
        Map<Long, InputStream> neuronsPointStreams = tmNeuronBufferDao.streamNeuronPointsByWorkspaceId(indexedWorkspaceNeurons.keySet(), workspace.getId());

        return workspaceNeurons.stream()
                .map(neuron -> ImmutablePair.of(neuron, neuronsPointStreams.get(neuron.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public void updateNeuronTagsTagsForNeurons(List<Long> neuronIds, List<String> tags, boolean tagState, String subjectKey) {
        ImmutableMap.Builder<String, EntityFieldValueHandler<?>> updatesBuilder = ImmutableMap.builder();
        if (tagState) {
            updatesBuilder.put("tags", new AppendFieldValueHandler<>(ImmutableSet.copyOf(tags)));
        } else {
            updatesBuilder.put("tags", new RemoveItemsFieldValueHandler<>(ImmutableSet.copyOf(tags)));
        }
        updatesBuilder.put("updatedDate", new SetFieldValueHandler<>(new Date()));
        Map<String, EntityFieldValueHandler<?>> updates = updatesBuilder.build();
        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(false);
        MongoDaoHelper.updateMany(
                mongoCollection,
                MongoDaoHelper.createFilterCriteria(
                        ImmutableList.of(
                                MongoDaoHelper.createFilterByIds(neuronIds),
                                permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey))
                ),
                updates,
                updateOptions);
    }
}
