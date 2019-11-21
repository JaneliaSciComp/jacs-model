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
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.dao.AppendFieldValueHandler;
import org.janelia.model.access.domain.dao.EntityFieldValueHandler;
import org.janelia.model.access.domain.dao.RemoveItemsFieldValueHandler;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.access.domain.dao.TmNeuronBufferDao;
import org.janelia.model.access.domain.dao.TmNeuronMetadataDao;
import org.janelia.model.domain.DomainUtils;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.tiledMicroscope.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TmNeuronMetadata} Mongo DAO.
 */
public class TmNeuronMetadataMongoDao extends AbstractDomainObjectMongoDao<TmNeuronMetadata> implements TmNeuronMetadataDao {
    private static final Logger LOG = LoggerFactory.getLogger(TmNeuronMetadataMongoDao.class);

    private final DomainDAO domainDao;
    private final TmNeuronBufferDao tmNeuronBufferDao;
    private final MongoDatabase mongoDatabase;
    private final DomainUpdateMongoHelper updateHelper;

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
        this.updateHelper = updateHelper;
        this.mongoDatabase = mongoDatabase;
        this.tmNeuronBufferDao = tmNeuronBufferDao;
    }

    @Override
    public TmNeuronMetadata createTmNeuronInWorkspace(String subjectKey, TmNeuronMetadata neuronMetadata, TmWorkspace workspace) {
        String neuronOwnerKey;
        String collection = workspace.getNeuronCollection();
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
            persistedNeuronMetadata = saveNeuron(neuronMetadata,collection, neuronOwnerKey);
            if (isLarge) {
                saveLargeNeuronPointData(persistedNeuronMetadata.getId(), pointData);
            }
            return persistedNeuronMetadata;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public TmNeuronMetadata getTmNeuronMetadata(String subjectKey, TmWorkspace workspace, Long neuronId) {
        return MongoDaoHelper.findById(neuronId, getNeuronCollection(workspace.getNeuronCollection()), TmNeuronMetadata.class);
    }

    <R> List<R> find(Bson queryFilter, Bson sortCriteria, long offset, int length, Class<R> resultType,
                     MongoCollection<TmNeuronMetadata> neuronCollection) {
        return MongoDaoHelper.find(queryFilter, sortCriteria, offset, length, neuronCollection, resultType);
    }

    @Override
    public List<TmNeuronMetadata> getTmNeuronMetadataByWorkspaceId(TmWorkspace workspace, String subjectKey,
                                                                   long offset, int length) {
        String workspaceRef = "TmWorkspace#" + workspace.getId();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<TmNeuronMetadata> neuronList = find(
                MongoDaoHelper.createFilterCriteria(
                        Filters.eq("workspaceRef", workspaceRef),
                        permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey)),
                null,
                offset,
                length,
                getEntityType(),
                getNeuronCollection(workspace.getNeuronCollection()));
         hydrateLargeNeurons(neuronList);

        LOG.info("BATCH TIME {} ms", stopWatch.getTime());
        stopWatch.stop();
         return neuronList;
    }

    @Override
    public List<TmNeuronMetadata> getTmNeuronMetadataByNeuronIds(TmWorkspace workspace, List<Long> neuronIdList) {
        return MongoDaoHelper.findByIds(neuronIdList, getNeuronCollection(workspace.getNeuronCollection()), TmNeuronMetadata.class);
    }

    private void saveLargeNeuronPointData (Long neuronId, TmNeuronData pointData) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            byte[] binaryData = mapper.writeValueAsBytes(pointData);
            gridFSMongoDao.updateDataBlock(new ByteArrayInputStream(binaryData),neuronId.toString());
        } catch (Exception e) {
            LOG.error ("Problem saving large neuron to GridFS",e);
            throw new RuntimeException("Problem saving large neuron to GridFS");
        }
    }

    private void removeLargeNeuronPointData (Long neuronId) {
        try {
            gridFSMongoDao.deleteDataBlock(neuronId.toString());
        } catch (Exception e) {

            LOG.error ("Problem saving large neuron to GridFS",e);
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
            LOG.error ("Problem checking the size of the neuron",e);
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

    @Override
    public boolean removeTmNeuron(Long neuronId, boolean isLarge, TmWorkspace workspace, String subjectKey) {
        if (isLarge) {
            gridFSMongoDao.deleteDataBlock(neuronId.toString());
        }
        long nDeleted = MongoDaoHelper.deleteMatchingRecords(getNeuronCollection(workspace.getNeuronCollection()),
                Filters.and(MongoDaoHelper.createFilterById(neuronId),
                        permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey)));
        if (nDeleted > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public TmNeuronMetadata saveNeuronMetadata(TmWorkspace workspace, TmNeuronMetadata neuron, String subjectKey) {
        boolean isLarge = checkLargeNeuron(neuron);
        TmNeuronData pointData = neuron.getNeuronData();
        if (isLarge) {
            neuron.setNeuronData(null);
            saveLargeNeuronPointData(neuron.getId(), pointData);
            if (!neuron.isLargeNeuron())
               removeTmNeuron(neuron.getId(),isLarge, workspace, subjectKey);
        } else {
            if (neuron.isLargeNeuron())
                removeLargeNeuronPointData(neuron.getId());
        }
        neuron.setLargeNeuron(isLarge);
        saveNeuron(neuron, workspace.getNeuronCollection(), subjectKey);
        if (isLarge)
            neuron.setNeuronData(pointData);
        return neuron;
    }

    private TmNeuronMetadata saveNeuron(TmNeuronMetadata entity,
                                       String collectionName, String subjectKey) {
        MongoCollection<TmNeuronMetadata> mongoCollection =  getNeuronCollection(collectionName);

        Date now = new Date();
        if (entity.getId() == null) {
            entity.setId(createNewId());
            for (TmGeoAnnotation anno: entity.getRootAnnotations()) {
                anno.setParentId(entity.getId());
            }
            entity.setOwnerKey(subjectKey);
            entity.getReaders().add(subjectKey);
            entity.getWriters().add(subjectKey);
            entity.setCreationDate(now);
            entity.setUpdatedDate(now);
            mongoCollection.insertOne(entity);
        } else {
            entity.setUpdatedDate(now);
            mongoCollection.updateOne(
                    Filters.and(MongoDaoHelper.createFilterById(entity.getId()),
                            permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey)),
                    updateHelper.getEntityUpdates(entity)
            );
        }
        return entity;
    }


    @Override
    public void updateNeuronStyles(BulkNeuronStyleUpdate bulkNeuronStyleUpdate, TmWorkspace workspace, String subjectKey) {
        MongoCollection<TmNeuronMetadata> mongoCollection =  getNeuronCollection(workspace.getNeuronCollection());
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
    public void removeEmptyNeuronsInWorkspace(TmWorkspace workspace, String subjectKey) {
        MongoCollection<TmNeuronMetadata> mongoCollection = getNeuronCollection(workspace.getNeuronCollection());

        MongoDaoHelper.deleteMatchingRecords(
                mongoCollection,
                MongoDaoHelper.createFilterCriteria(
                        ImmutableList.of(
                                Filters.eq("workspaceRef", "TmWorkspace#" + workspace.getId()),
                                Filters.exists("neuronData", false),
                                permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey))
                ));
    }

    @Override
    public void bulkMigrateNeuronsInWorkspace(TmWorkspace workspace, Collection<TmNeuronMetadata> neurons, String subjectKey) {
        String collectionName = MongoDaoHelper.findOrCreateCappedCollection (this, mongoDatabase,
                "tmNeuron", 20000000000L, TmNeuronMetadata.class);
        try {
            workspace.setNeuronCollection(collectionName);
            workspace = domainDao.save(subjectKey, workspace);
        } catch (Exception e) {
            LOG.error("ERROR SAVING WORKSPACE {} - Issue with Neuron Collection Key", workspace.getId(),e);
            throw new RuntimeException("ERROR SAVING WORKSPACE" + workspace.getId() + " - Issue with Neuron Collection Key");
        }

        MongoCollection<TmNeuronMetadata> copyLoc = getNeuronCollection(collectionName);
        if (neurons==null || neurons.isEmpty())
            return;
        List<TmNeuronMetadata> neuronList = new ArrayList<>(neurons);
        try {
            int prevCount = 0;
            for (int i=0; i<neuronList.size(); i=i+1000) {
                if (neuronList.size()>(i+1000)) {
                    copyLoc.insertMany(neuronList.subList(i, i + 1000));
                    LOG.info("Inserted {} neurons", i+1000);
                }
                else {
                    copyLoc.insertMany(neuronList.subList(i, neuronList.size()));
                    LOG.info("Inserted {} neurons", neuronList.size());
                }
            }
            LOG.info("Finished workspace {}",workspace.getId());
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
                LOG.error("ERROR PROCESSING {} - Issue with Large Neurons", workspace.getId(),e);
            }
        }
    }

    @Override
    public List<Pair<TmNeuronMetadata, InputStream>> getTmNeuronsMetadataWithPointStreamsByWorkspaceId(TmWorkspace workspace,
                                                                                                       String subjectKey,
                                                                                                       long offset, int length) {
        List<TmNeuronMetadata> workspaceNeurons = getTmNeuronMetadataByWorkspaceId(workspace,subjectKey, offset, length);
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
    public void updateNeuronTagsForNeurons(TmWorkspace workspace, List<Long> neuronIds, List<String> tags, boolean tagState,
                                            String subjectKey) {
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
                getNeuronCollection(workspace.getNeuronCollection()),
                MongoDaoHelper.createFilterCriteria(
                        ImmutableList.of(
                                MongoDaoHelper.createFilterByIds(neuronIds),
                                permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey))
                ),
                updates,
                updateOptions);
    }

    @Override
    public Long getNeuronCountsForWorkspace(TmWorkspace workspace, String subjectKey) {
        MongoCollection<TmNeuronMetadata> mongoCollection = getNeuronCollection(workspace.getNeuronCollection());
        return MongoDaoHelper.count(
                MongoDaoHelper.createFilterCriteria(
                Filters.eq("workspaceRef", Reference.createFor(workspace)),
                permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey)),
                mongoCollection
        );
    }

    private MongoCollection<TmNeuronMetadata> getNeuronCollection(String collectionName) {
        return mongoDatabase.getCollection(collectionName, TmNeuronMetadata.class);
    }
}
