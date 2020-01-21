package org.janelia.model.access.domain.dao.mongo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.dao.AppendFieldValueHandler;
import org.janelia.model.access.domain.dao.EntityFieldValueHandler;
import org.janelia.model.access.domain.dao.RemoveItemsFieldValueHandler;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.access.domain.dao.TmNeuronBufferDao;
import org.janelia.model.access.domain.dao.TmNeuronDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.tiledMicroscope.*;
import org.janelia.model.domain.tiledMicroscope.TmNeuronSkeletons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TmNeuron} Mongo DAO.
 */
public class TmNeuronMongoDao extends AbstractDomainObjectMongoDao<TmNeuron> implements TmNeuronDao {
    private static final Logger LOG = LoggerFactory.getLogger(TmNeuronMongoDao.class);

    private final DomainDAO domainDao;
    private final TmNeuronBufferDao tmNeuronBufferDao;
    private final MongoDatabase mongoDatabase;
    private final DomainUpdateMongoHelper updateHelper;

    @Inject
    private GridFSMongoDao gridFSMongoDao;

    @Inject
    TmNeuronMongoDao(MongoDatabase mongoDatabase,
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
    public TmNeuron createTmNeuronInWorkspace(String subjectKey, TmNeuron neuronMetadata, TmWorkspace workspace) {
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
        TmNeuron persistedNeuronMetadata;
        try {
            boolean isLarge = checkLargeNeuron(neuronMetadata);
            TmNeuronSkeletons pointData = neuronMetadata.getAnnotations();
            if (isLarge) {
                neuronMetadata.setAnnotations(null);
                neuronMetadata.getPersistence().setLargeNeuron(true);
            }
            persistedNeuronMetadata = saveNeuron(neuronMetadata,collection, neuronOwnerKey, false);
            if (isLarge) {
                saveLargeNeuronPointData(persistedNeuronMetadata.getId(), pointData);
            }
            return persistedNeuronMetadata;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public TmNeuron getTmNeuron(String subjectKey, TmWorkspace workspace, Long neuronId) {
        TmNeuron neuron = MongoDaoHelper.findById(neuronId, getNeuronCollection(workspace.getNeuronCollection()), TmNeuron.class);
        AsyncPersistence persistence = neuron.getPersistence();
        if (persistence.isLargeNeuron()) {
            List<TmNeuron> list = new ArrayList<TmNeuron>();
            list.add(neuron);
            hydrateLargeNeurons(list);
        }
        return neuron;
    }

    <R> List<R> find(Bson queryFilter, Bson sortCriteria, long offset, int length, Class<R> resultType,
                     MongoCollection<TmNeuron> neuronCollection) {
        return MongoDaoHelper.find(queryFilter, sortCriteria, offset, length, neuronCollection, resultType);
    }

    @Override
    public List<TmNeuron> getTmNeuronByWorkspaceId(TmWorkspace workspace, String subjectKey,
                                                                   long offset, int length) {
        String workspaceRef = "TmWorkspace#" + workspace.getId();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<TmNeuron> neuronList = find(
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
    public Iterable<TmNeuron> streamWorkspaceNeurons(TmWorkspace workspace, String subjectKey,
                                                             long offset, int length) {
        String workspaceRef = "TmWorkspace#" + workspace.getId();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        FindIterable<TmNeuron> neuronList =
                MongoDaoHelper.rawFind(
                        MongoDaoHelper.createFilterCriteria(
                                Filters.eq("workspaceRef", workspaceRef),
                                permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey)),
                        offset,
                        length,
                        getNeuronCollection(workspace.getNeuronCollection()),
                        TmNeuron.class
                );
        LOG.info("BATCH TIME {} ms", stopWatch.getTime());
        stopWatch.stop();
        return neuronList;
    }

    @Override
    public List<TmNeuron> getTmNeuronByNeuronIds(TmWorkspace workspace, List<Long> neuronIdList) {
        return MongoDaoHelper.findByIds(neuronIdList, getNeuronCollection(workspace.getNeuronCollection()), TmNeuron.class);
    }

    private void saveLargeNeuronPointData (Long neuronId, TmNeuronSkeletons pointData) {
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


    private boolean checkLargeNeuron (TmNeuron neuron) {
        ObjectMapper mapper = new ObjectMapper();
        int NODE_LIMIT = 40000;
        // apply quick check on node count to speed up performance
        if (neuron.getAnnotations().getGeoAnnotations().size()<NODE_LIMIT &&
                neuron.getAnnotations().getAnchoredPaths().size()<NODE_LIMIT)
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

    private void hydrateLargeNeurons(List<TmNeuron> neuronList) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            for (TmNeuron neuron: neuronList) {
                // hydrate large neurons from gridfs
                if (neuron.getPersistence().isLargeNeuron()) {
                    ByteArrayOutputStream outputNeuron = new ByteArrayOutputStream();
                    gridFSMongoDao.downloadDataBlock(outputNeuron, neuron.getId().toString());
                    TmNeuronSkeletons pointData = mapper.readValue(outputNeuron.toByteArray(), TmNeuronSkeletons.class);
                    neuron.setAnnotations(pointData);
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
    public TmNeuron saveNeuronMetadata(TmWorkspace workspace, TmNeuron neuron, String subjectKey) {
        boolean isLarge = checkLargeNeuron(neuron);
        TmNeuronSkeletons pointData = neuron.getAnnotations();
        if (isLarge) {
            neuron.setAnnotations(null);
            if (!neuron.getPersistence().isLargeNeuron()) {
                removeTmNeuron(neuron.getId(), isLarge, workspace, subjectKey);
                neuron.getPersistence().setLargeNeuron(true);
                saveNeuron(neuron, workspace.getNeuronCollection(), subjectKey, true);
            } else {
                saveNeuron(neuron, workspace.getNeuronCollection(), subjectKey, false);
            }
            saveLargeNeuronPointData(neuron.getId(), pointData);
        } else {
            if (neuron.getPersistence().isLargeNeuron())
                removeLargeNeuronPointData(neuron.getId());
            neuron.getPersistence().setLargeNeuron(isLarge);
            saveNeuron(neuron, workspace.getNeuronCollection(), subjectKey, false);
        }
        if (isLarge)
            neuron.setAnnotations(pointData);
        return neuron;
    }

    private TmNeuron saveNeuron(TmNeuron entity,
                                       String collectionName, String subjectKey, boolean forceCreate) {
        MongoCollection<TmNeuron> mongoCollection =  getNeuronCollection(collectionName);

        Date now = new Date();
        if (entity.getId() == null || forceCreate) {
            if (!forceCreate)
                entity.setId(createNewId());
            if (entity.getAnnotations()!=null) {
                for (TmNeuronAnnotation anno : entity.getRootAnnotations()) {
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
        MongoCollection<TmNeuron> mongoCollection =  getNeuronCollection(workspace.getNeuronCollection());
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
        MongoCollection<TmNeuron> mongoCollection = getNeuronCollection(workspace.getNeuronCollection());

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
    public void bulkMigrateNeuronsInWorkspace(TmWorkspace workspace, Collection<TmNeuron> neurons, String subjectKey) {
        String collectionName = MongoDaoHelper.findOrCreateCappedCollection (this, mongoDatabase,
                "tmNeuron", 20000000000L, TmNeuron.class);
        try {
            workspace.setNeuronCollection(collectionName);
            workspace = domainDao.save(subjectKey, workspace);
        } catch (Exception e) {
            LOG.error("ERROR SAVING WORKSPACE {} - Issue with Neuron Collection Key", workspace.getId(),e);
            throw new RuntimeException("ERROR SAVING WORKSPACE" + workspace.getId() + " - Issue with Neuron Collection Key");
        }

        MongoCollection<TmNeuron> copyLoc = getNeuronCollection(collectionName);
        if (neurons==null || neurons.isEmpty())
            return;
        List<TmNeuron> neuronList = new ArrayList<>(neurons);
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
            List<TmNeuron> truncatedList = new ArrayList<>();
            boolean hitFirstLarge = false;
            for (TmNeuron neuron: neuronList) {
                if (hitFirstLarge)
                    truncatedList.add(neuron);
                // remove records that were saved before batch failed
                MongoDaoHelper.deleteMatchingRecords(copyLoc,
                        Filters.and(MongoDaoHelper.createFilterById(neuron.getId()),
                                permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey)));
                boolean isLarge = checkLargeNeuron(neuron);
                TmNeuronSkeletons pointData = neuron.getAnnotations();
                if (isLarge) {
                    hitFirstLarge = true;
                    largeNeurons.add(neuron.getId());
                    neuron.setAnnotations(null);
                    saveLargeNeuronPointData(neuron.getId(), pointData);
                    neuron.getPersistence().setLargeNeuron(isLarge);
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
        MongoCollection<TmNeuron> mongoCollection = getNeuronCollection(workspace.getNeuronCollection());
        return MongoDaoHelper.count(
                MongoDaoHelper.createFilterCriteria(
                Filters.eq("workspaceRef", Reference.createFor(workspace)),
                permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey)),
                mongoCollection
        );
    }

    private MongoCollection<TmNeuron> getNeuronCollection(String collectionName) {
        return mongoDatabase.getCollection(collectionName, TmNeuron.class);
    }
}
