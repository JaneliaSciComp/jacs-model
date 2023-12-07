package org.janelia.model.access.domain.dao.mongo;

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
import org.janelia.model.access.domain.dao.*;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.tiledMicroscope.*;
import org.janelia.model.security.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * {@link TmNeuronMetadata} Mongo DAO.
 */
public class TmNeuronMetadataMongoDao extends AbstractDomainObjectMongoDao<TmNeuronMetadata> implements TmNeuronMetadataDao {

    private static final Logger LOG = LoggerFactory.getLogger(TmNeuronMetadataMongoDao.class);

    private final MongoDatabase mongoDatabase;
    private final DomainUpdateMongoHelper updateHelper;

    @Inject
    private GridFSMongoDao gridFSMongoDao;

    @Inject
    TmNeuronMetadataMongoDao(MongoDatabase mongoDatabase,
                             TimebasedIdentifierGenerator idGenerator,
                             DomainPermissionsMongoHelper permissionsHelper,
                             DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
        this.updateHelper = updateHelper;
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    public TmNeuronMetadata createTmNeuronInWorkspace(String subjectKey, TmNeuronMetadata neuronMetadata, TmWorkspace workspace) {
        String neuronOwnerKey;
        String collection = workspace.getNeuronCollection();
        // check permissions
        Set<String> subjectWriteGroups = permissionsHelper.retrieveSubjectWriteGroups(subjectKey);
        if (!subjectKey.equals(workspace.getOwnerKey()) &&
                !subjectWriteGroups.contains(Subject.ADMIN_KEY) &&
                !CollectionUtils.containsAny(workspace.getWriters(), subjectWriteGroups)) {
            // the current subject is neither an owner nor an allowed writer to the current workspace
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
            persistedNeuronMetadata = saveNeuron(neuronMetadata,collection, neuronOwnerKey, true);
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
                                nofrags ? null : Filters.or(
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
    public void createOperationLog(Long workspaceId, Long neuronId, String operationType, Date timestamp, String subjectKey) {
        MongoCollection<TmOperation> operationCollection =  mongoDatabase.getCollection("tmOperation", TmOperation.class);

        TmOperation operation = new TmOperation();
        operation.setUser(subjectKey);
        operation.setWorkspaceId(workspaceId);
        operation.setNeuronId(neuronId);
        operation.setOperation(operationType);
        operation.setTimestamp(timestamp);
        operationCollection.insertOne(operation);
    }

    @Override
    public List<TmOperation> getOperations(Long workspaceId, Long neuronId, Date startDate, Date endDate) {
        MongoCollection<TmOperation> operationCollection =  mongoDatabase.getCollection("tmOperation", TmOperation.class);
        ImmutableList.Builder<Bson> operationFilterBuilder = ImmutableList.builder();
        operationFilterBuilder.add(Filters.eq("workspaceId", workspaceId));
        if (neuronId!=null) {
            operationFilterBuilder.add(Filters.eq("neuronId", neuronId));
        }
        if (startDate!=null) {
            operationFilterBuilder.add(Filters.and(Filters.gte("timestamp",startDate)));
        }
        if (endDate!=null) {
            operationFilterBuilder.add(Filters.and(Filters.lte("timestamp",endDate)));
        }
        Bson filter = MongoDaoHelper.createFilterCriteria(operationFilterBuilder.build());
        return MongoDaoHelper.find(filter, null, 0, 10000, operationCollection, TmOperation.class);
    }

    private TmNeuronMetadata saveNeuron(TmNeuronMetadata entity,
                                       String collectionName, String subjectKey, boolean forceCreate) {
        MongoCollection<TmNeuronMetadata> mongoCollection =  getNeuronCollection(collectionName);

        Date now = new Date();
        if (entity.getId() == null || forceCreate) {
            if (!forceCreate)
                entity.setId(createNewId());
            if (entity.getNeuronData()!=null) {
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
                throw new MongoException("Could not update "+entity+". Object was not matched.");
            }
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
            throw new MongoException("Update neuron styles failed. Tried to update "+desired+" neurons, but only found "+found);
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
            throw new MongoException("Update neuron tags failed. Tried to update "+desired+" neurons, but only found "+found);
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
        return MongoDaoHelper.deleteMatchingRecords(mongoCollection,
                Filters.and(MongoDaoHelper.createFilterCriteria(
                        Filters.eq("workspaceRef", Reference.createFor(workspace))
                ), permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey)));
    }

    private MongoCollection<TmNeuronMetadata> getNeuronCollection(String collectionName) {
        return mongoDatabase.getCollection(collectionName, TmNeuronMetadata.class);
    }
}
