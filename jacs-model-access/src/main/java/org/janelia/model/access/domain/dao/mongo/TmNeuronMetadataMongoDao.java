package org.janelia.model.access.domain.dao.mongo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.AppendFieldValueHandler;
import org.janelia.model.access.domain.dao.DaoUpdateResult;
import org.janelia.model.access.domain.dao.EntityFieldValueHandler;
import org.janelia.model.access.domain.dao.RemoveItemsFieldValueHandler;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.access.domain.dao.TmNeuronMetadataDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.tiledMicroscope.BulkNeuronStyleUpdate;
import org.janelia.model.domain.tiledMicroscope.TmGeoAnnotation;
import org.janelia.model.domain.tiledMicroscope.TmNeuronData;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmOperation;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;
import org.janelia.model.security.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TmNeuronMetadata} Mongo DAO.
 */
public class TmNeuronMetadataMongoDao extends AbstractDomainObjectMongoDao<TmNeuronMetadata> implements TmNeuronMetadataDao {

    private static final Logger LOG = LoggerFactory.getLogger(TmNeuronMetadataMongoDao.class);

    private final MongoDatabase mongoDatabase;
    private final DomainUpdateMongoHelper updateHelper;
    private final GridFSMongoDao gridFSMongoDao;

    @Inject
    TmNeuronMetadataMongoDao(MongoDatabase mongoDatabase,
                             TimebasedIdentifierGenerator idGenerator,
                             DomainPermissionsMongoHelper permissionsHelper,
                             DomainUpdateMongoHelper updateHelper,
                             GridFSMongoDao gridFSMongoDao) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
        this.updateHelper = updateHelper;
        this.mongoDatabase = mongoDatabase;
        this.gridFSMongoDao = gridFSMongoDao;
    }

    @Override
    public TmNeuronMetadata createTmNeuronInWorkspace(String subjectKey, TmNeuronMetadata neuronMetadata, TmWorkspace workspace) {
        String neuronOwnerKey;
        String collection = workspace.getNeuronCollection();
        // check permissions
        Set<String> subjectWriteGroups = permissionsHelper.retrieveSubjectWriteGroups(subjectKey);
        if (!subjectKey.equals(workspace.getOwnerKey()) &&
                !subjectWriteGroups.contains(Subject.ADMIN_KEY) &&
                !CollectionUtils.containsAny(workspace.getWriters(), subjectWriteGroups) &&
                !workspace.getWriters().contains(subjectKey)
        ) {
            // the current subject is neither an owner nor an allowed writer to the current workspace
            LOG.info("{} is not an allowed writer - {} to workspace {}:{}",
                    subjectKey, workspace.getWriters(), workspace.getId(), workspace.getName());
            throw new SecurityException(subjectKey + " is not allowed to write to workspace " + workspace.getName());
        }
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
            LOG.debug("creating neuron");
            if (neuronMetadata.getId() != null) {
                LOG.debug("Recreating existing neuron with id {} of class type {}",
                        neuronMetadata.getId(), neuronMetadata.getId().getClass());
                persistedNeuronMetadata = createNeuronWithExistingId(neuronMetadata, collection, neuronOwnerKey);
            } else
                persistedNeuronMetadata = saveNeuron(neuronMetadata, collection, neuronOwnerKey, false);
            if (isLarge) {
                saveLargeNeuronPointData(persistedNeuronMetadata.getId(), pointData);
            }
            return persistedNeuronMetadata;
        } catch (Exception e) {
            LOG.error("Error creating neuron {} in workspace:{}:{}",
                    neuronMetadata, workspace, workspace.getName(), e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public TmNeuronMetadata getTmNeuronMetadata(String subjectKey, TmWorkspace workspace, Long neuronId) {
        TmNeuronMetadata neuron = MongoDaoHelper.findById(neuronId, getNeuronCollection(workspace.getNeuronCollection()), TmNeuronMetadata.class);
        if (neuron.isLargeNeuron()) {
            List<TmNeuronMetadata> list = new ArrayList<>();
            list.add(neuron);
            hydrateLargeNeurons(list);
        }
        return neuron;
    }

    <R> List<R> find(Bson queryFilter, Bson sortCriteria, long offset, int length, Class<R> resultType,
                     MongoCollection<TmNeuronMetadata> neuronCollection) {
        return MongoDaoHelper.find(queryFilter, sortCriteria, offset, length, neuronCollection, resultType);
    }

    @Override
    public List<TmNeuronMetadata> getTmNeuronMetadataByWorkspaceId(TmWorkspace workspace, String subjectKey,
                                                                   long offset, int length, boolean nofrags) {
        String workspaceRef = "TmWorkspace#" + workspace.getId();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<TmNeuronMetadata> neuronList = find(
                MongoDaoHelper.createFilterCriteria(
                        !nofrags ? null : Filters.or(
                                Filters.exists("fragment", false),
                                Filters.eq("filter", false)
                        ),
                        Filters.eq("workspaceRef", workspaceRef),
                        permissionsHelper.createSameGroupReadPermissionFilterForSubjectKey(subjectKey)),
                null,
                offset,
                length,
                getEntityType(),
                getNeuronCollection(workspace.getNeuronCollection()));
        hydrateLargeNeurons(neuronList);

        LOG.trace("BATCH TIME {} ms", stopWatch.getTime());
        stopWatch.stop();
        return neuronList;
    }

    @Override
    public Iterable<TmNeuronMetadata> streamWorkspaceNeurons(TmWorkspace workspace, String subjectKey,
                                                             long offset, int length, boolean nofrags) {
        String workspaceRef = "TmWorkspace#" + workspace.getId();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        FindIterable<TmNeuronMetadata> neuronList =
                MongoDaoHelper.rawFind(
                        MongoDaoHelper.createFilterCriteria(
                                !nofrags ? null : Filters.or(
                                        Filters.exists("fragment", false),
                                        Filters.eq("fragment", false)
                                ),
                                Filters.eq("workspaceRef", workspaceRef),
                                permissionsHelper.createSameGroupReadPermissionFilterForSubjectKey(subjectKey)),
                        null,
                        offset,
                        length,
                        getNeuronCollection(workspace.getNeuronCollection()),
                        TmNeuronMetadata.class
                );
        LOG.trace("BATCH TIME {} ms", stopWatch.getTime());
        stopWatch.stop();
        return neuronList;
    }

    @Override
    public List<TmNeuronMetadata> getTmNeuronMetadataByNeuronIds(TmWorkspace workspace, List<Long> neuronIdList) {
        return MongoDaoHelper.findByIds(neuronIdList, getNeuronCollection(workspace.getNeuronCollection()), TmNeuronMetadata.class);
    }

    private void saveLargeNeuronPointData(Long neuronId, TmNeuronData pointData) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            byte[] binaryData = mapper.writeValueAsBytes(pointData);
            gridFSMongoDao.updateDataBlock(new ByteArrayInputStream(binaryData), neuronId.toString());
        } catch (Exception e) {
            LOG.error("Problem saving large neuron to GridFS", e);
            throw new RuntimeException("Problem saving large neuron to GridFS");
        }
    }

    private void removeLargeNeuronPointData(Long neuronId) {
        try {
            gridFSMongoDao.deleteDataBlock(neuronId.toString());
        } catch (Exception e) {

            LOG.error("Problem saving large neuron to GridFS", e);
            throw new RuntimeException("Problem removing large neuron from GridFS");
        }
    }


    private boolean checkLargeNeuron(TmNeuronMetadata neuron) {
        ObjectMapper mapper = new ObjectMapper();
        int NODE_LIMIT = 40000;
        // apply quick check on node count to speed up performance
        if (neuron.getNeuronData().getGeoAnnotations().size() < NODE_LIMIT &&
                neuron.getNeuronData().getAnchoredPaths().size() < NODE_LIMIT)
            return false;
        try {
            if (mapper.writeValueAsBytes(neuron).length > 10000000) {
                return true;
            }
        } catch (JsonProcessingException e) {
            LOG.error("Problem checking the size of the neuron", e);
            throw new RuntimeException("Problem checking the size of neuron "
                    + neuron.getId());
        }
        return false;
    }

    private void hydrateLargeNeurons(List<TmNeuronMetadata> neuronList) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            for (TmNeuronMetadata neuron : neuronList) {
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
            if (!neuron.isLargeNeuron()) {
                removeTmNeuron(neuron.getId(), isLarge, workspace, subjectKey);
                neuron.setLargeNeuron(true);
                saveNeuron(neuron, workspace.getNeuronCollection(), subjectKey, true);
            } else {
                saveNeuron(neuron, workspace.getNeuronCollection(), subjectKey, false);
            }
            saveLargeNeuronPointData(neuron.getId(), pointData);
        } else {
            if (neuron.isLargeNeuron())
                removeLargeNeuronPointData(neuron.getId());
            neuron.setLargeNeuron(isLarge);
            saveNeuron(neuron, workspace.getNeuronCollection(), subjectKey, false);
        }
        if (isLarge)
            neuron.setNeuronData(pointData);
        return neuron;
    }

    @Override
    public void createOperationLog(Long sampleId, Long workspaceId, Long neuronId, TmOperation.Activity operationType, Date timestamp, Long elapsedTime,
                                   String subjectKey) {
        MongoCollection<TmOperation> operationCollection = mongoDatabase.getCollection("tmOperation", TmOperation.class);

        TmOperation operation = new TmOperation();
        operation.setUser(subjectKey);
        operation.setSampleId(sampleId);
        operation.setWorkspaceId(workspaceId);
        if (neuronId != null)
            operation.setNeuronId(neuronId);
        operation.setActivity(operationType);
        operation.setTimestamp(new Date());
        operation.setElapsedTime(elapsedTime);
        operationCollection.insertOne(operation);
    }

    @Override
    public List<TmOperation> getOperations(Long workspaceId, Long neuronId, Date startDate, Date endDate) {
        MongoCollection<TmOperation> operationCollection = mongoDatabase.getCollection("tmOperation", TmOperation.class);
        ImmutableList.Builder<Bson> operationFilterBuilder = ImmutableList.builder();
        operationFilterBuilder.add(Filters.eq("workspaceId", workspaceId));
        if (neuronId != null) {
            operationFilterBuilder.add(Filters.eq("neuronId", neuronId));
        }
        if (startDate != null) {
            operationFilterBuilder.add(Filters.and(Filters.gte("timestamp", startDate)));
        }
        if (endDate != null) {
            operationFilterBuilder.add(Filters.and(Filters.lte("timestamp", endDate)));
        }
        Bson filter = MongoDaoHelper.createFilterCriteria(operationFilterBuilder.build());
        return MongoDaoHelper.find(filter, null, 0, 10000, operationCollection, TmOperation.class);
    }

    private TmNeuronMetadata createNeuronWithExistingId(TmNeuronMetadata entity,
                                                        String collectionName, String subjectKey) {
        MongoCollection<TmNeuronMetadata> mongoCollection = getNeuronCollection(collectionName);

        Date now = new Date();
        for (TmGeoAnnotation anno : entity.getRootAnnotations()) {
            anno.setParentId(entity.getId());
        }
        entity.setOwnerKey(subjectKey);
        entity.getReaders().add(subjectKey);
        entity.getWriters().add(subjectKey);
        entity.setCreationDate(now);
        entity.setUpdatedDate(now);
        mongoCollection.insertOne(entity);
        return entity;
    }

    private TmNeuronMetadata saveNeuron(TmNeuronMetadata entity,
                                        String collectionName, String subjectKey, boolean forceCreate) {
        MongoCollection<TmNeuronMetadata> mongoCollection = getNeuronCollection(collectionName);

        Date now = new Date();
        if (entity.getId() == null || forceCreate) {
            if (!forceCreate)
                entity.setId(createNewId());
            if (entity.getNeuronData() != null) {
                for (TmGeoAnnotation anno : entity.getRootAnnotations()) {
                    anno.setParentId(entity.getId());
                }
            }
            entity.setOwnerKey(subjectKey);
            entity.getReaders().add(subjectKey);
            entity.getWriters().add(subjectKey);
            entity.setCreationDate(now);
            entity.setUpdatedDate(now);
            mongoCollection.insertOne(entity);
        } else {
            entity.setUpdatedDate(now);
            UpdateResult updateResult = mongoCollection.updateOne(
                    Filters.and(MongoDaoHelper.createFilterById(entity.getId()),
                            permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey)),
                    updateHelper.getEntityUpdates(entity)
            );
            if (updateResult.getMatchedCount() == 0) {
                throw new MongoException("Could not update " + entity + ". Object was not matched.");
            }
        }
        return entity;
    }


    @Override
    public void updateNeuronStyles(BulkNeuronStyleUpdate bulkNeuronStyleUpdate, TmWorkspace workspace, String subjectKey) {
        MongoCollection<TmNeuronMetadata> mongoCollection = getNeuronCollection(workspace.getNeuronCollection());
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
        DaoUpdateResult daoUpdateResult = MongoDaoHelper.updateMany(
                mongoCollection,
                MongoDaoHelper.createFilterCriteria(
                        ImmutableList.of(
                                MongoDaoHelper.createFilterByIds(bulkNeuronStyleUpdate.getNeuronIds()),
                                permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey))
                ),
                updates,
                updateOptions);
        long desired = bulkNeuronStyleUpdate.getNeuronIds().size();
        long found = daoUpdateResult.getEntitiesFound();
        if (desired != found) {
            throw new MongoException("Update neuron styles failed. Tried to update " + desired + " neurons, but only found " + found);
        }
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
        DaoUpdateResult daoUpdateResult = MongoDaoHelper.updateMany(
                getNeuronCollection(workspace.getNeuronCollection()),
                MongoDaoHelper.createFilterCriteria(
                        ImmutableList.of(
                                MongoDaoHelper.createFilterByIds(neuronIds),
                                permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey))
                ),
                updates,
                updateOptions);
        long desired = neuronIds.size();
        long found = daoUpdateResult.getEntitiesFound();
        if (desired != found) {
            throw new MongoException("Update neuron tags failed. Tried to update " + desired + " neurons, but only found " + found);
        }
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

    @Override
    public long deleteNeuronsForWorkspace(TmWorkspace workspace, String subjectKey) {
        LOG.info("Deleting neurons from workspace {} in mongo collection {}",
                workspace.getName(), workspace.getNeuronCollection());
        return MongoDaoHelper.deleteMatchingRecords(
                getNeuronCollection(workspace.getNeuronCollection()),
                Filters.eq("workspaceRef", Reference.createFor(workspace))  // Simplified filter
        );
    }

    private MongoCollection<TmNeuronMetadata> getNeuronCollection(String collectionName) {
        return mongoDatabase.getCollection(collectionName, TmNeuronMetadata.class);
    }
}
