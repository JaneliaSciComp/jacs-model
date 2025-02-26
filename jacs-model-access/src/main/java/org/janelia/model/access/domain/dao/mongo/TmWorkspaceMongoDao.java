package org.janelia.model.access.domain.dao.mongo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;

import com.mongodb.client.model.Projections;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.dao.TmMappedNeuronDao;
import org.janelia.model.access.domain.dao.TmNeuronMetadataDao;
import org.janelia.model.access.domain.dao.TmWorkspaceDao;
import org.janelia.model.domain.DomainConstants;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.tiledMicroscope.*;
import org.janelia.model.domain.workspace.TreeNode;
import org.janelia.model.util.SortCriteria;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TmWorkspace} Mongo DAO.
 */
public class TmWorkspaceMongoDao extends AbstractDomainObjectMongoDao<TmWorkspace> implements TmWorkspaceDao {
    private static final Logger LOG = LoggerFactory.getLogger(TmWorkspaceMongoDao.class);

    private final MongoDatabase mongoDatabase;
    private final DomainDAO domainDao;
    private final TmNeuronMetadataDao tmNeuronMetadataDao;
    private final TmMappedNeuronDao tmMappedNeuronDao;

    @Inject
    private GridFSMongoDao gridFSMongoDao;

    @Inject
    TmWorkspaceMongoDao(MongoDatabase mongoDatabase,
                        TimebasedIdentifierGenerator idGenerator,
                        DomainPermissionsMongoHelper permissionsHelper,
                        DomainUpdateMongoHelper updateHelper,
                        DomainDAO domainDao,
                        TmNeuronMetadataDao tmNeuronMetadataDao,
                        TmMappedNeuronDao tmMappedNeuronDao) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
        this.mongoDatabase = mongoDatabase;
        this.domainDao = domainDao;
        this.tmNeuronMetadataDao = tmNeuronMetadataDao;
        this.tmMappedNeuronDao = tmMappedNeuronDao;
    }

    @Override
    public List<TmWorkspace> getTmWorkspacesForSample(String subjectKey, Long sampleId) {
        if (StringUtils.isBlank(subjectKey))
            return Collections.emptyList();
        return MongoDaoHelper.find(
                MongoDaoHelper.createFilterCriteria(
                        Filters.eq("sampleRef", Reference.createFor(TmSample.class, sampleId).toString()),
                        permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey)),
                MongoDaoHelper.createBsonSortCriteria(
                        new SortCriteria("ownerKey"),
                        new SortCriteria("_id")),
                0,
                -1,
                mongoCollection,
                TmWorkspace.class);
    }

    @Override
    public List<TmWorkspace> getAllTmWorkspaces(String subjectKey) {
        if (StringUtils.isBlank(subjectKey))
            return Collections.emptyList();
        return MongoDaoHelper.find(
                MongoDaoHelper.createFilterCriteria(
                        permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey)),
                MongoDaoHelper.createBsonSortCriteria(new SortCriteria("_id")),
                0,
                -1,
                mongoCollection,
                TmWorkspace.class);
    }

    @Override
    public Map<TmWorkspace, Long> getLargestWorkspaces(String subjectKey, Long limit) {
        // Step 1: Get all accessible workspaces using existing method
        List<TmWorkspace> workspaces = getAllTmWorkspaces(subjectKey);
        LOG.info("Retrieved {} workspaces for analysis", workspaces.size());

        Map<TmWorkspace, Long> workspaceSizeMap = new HashMap<>();

        // Step 2: Iterate through workspaces and aggregate neuron document sizes
        for (TmWorkspace workspace : workspaces) {
            String neuronCollectionName = workspace.getNeuronCollection();

            LOG.info("Analyzing {} workspace storing neurons in {}",
                    workspace.getName(),neuronCollectionName);
            if (neuronCollectionName == null || neuronCollectionName.isEmpty()) {
                continue; // Skip workspaces without a valid neuron collection
            }

            MongoCollection<Document> neuronCollection = mongoDatabase.getCollection(neuronCollectionName);

            // Aggregate to calculate the total size of neuron documents without fetching them
            AggregateIterable<Document> aggregation = neuronCollection.aggregate(Arrays.asList(
                    Aggregates.match(new Document("workspaceRef", "TmWorkspace#"+workspace.getId().toString())), // Add this line
                    Aggregates.project(Projections.fields(
                            Projections.computed("size", new Document("$bsonSize", "$$ROOT"))
                    )),
                    Aggregates.group(null, Accumulators.sum("totalSize", "$size"))
            ));

            LOG.info("Finished aggregation analysis");
            // Extract the total size from the aggregation result
            Long totalSize = 0L;
            for (Document result : aggregation) {
                totalSize = result.getLong("totalSize");
            }

            workspaceSizeMap.put(workspace, totalSize);
        }

        LOG.info("Workspace analysis complete. Results: {}",
                workspaceSizeMap);
        // Sort by total size in descending order and limit results
        return workspaceSizeMap.entrySet().stream()
                .sorted(Map.Entry.<TmWorkspace, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    @Override
    public TmWorkspace createTmWorkspace(String subjectKey, TmWorkspace tmWorkspace) {
        try {
            // find a suitable collection to store this workspace's neurons
            String collectionName = MongoDaoHelper.findOrCreateCappedCollection (this, mongoDatabase,
                    "tmNeuron", 20000000000L, TmNeuronMetadata.class);
            tmWorkspace.setNeuronCollection(collectionName);

            TmWorkspace workspace = domainDao.save(subjectKey, tmWorkspace);
            TreeNode folder = domainDao.getOrCreateDefaultTreeNodeFolder(subjectKey, DomainConstants.NAME_TM_WORKSPACE_FOLDER);
            domainDao.addChildren(subjectKey, folder, Collections.singletonList(Reference.createFor(workspace)));
            return workspace;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void saveWorkspaceBoundingBoxes(TmWorkspace workspace, List<BoundingBox3d> boundingBoxes) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            byte[] binaryData = mapper.writeValueAsBytes(boundingBoxes);
            gridFSMongoDao.updateDataBlock(new ByteArrayInputStream(binaryData), workspace.getId().toString());
        } catch (Exception e) {
            LOG.error ("Problem saving precomputed fragment bounding boxes to GridFS",e);
            throw new RuntimeException("Problem saving fragment bounding boxes to GridFS");
        }
    }

    @Override
    public List<BoundingBox3d> getWorkspaceBoundingBoxes(Long workspaceId) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            ByteArrayOutputStream boundingBoxBytes = new ByteArrayOutputStream();
            gridFSMongoDao.downloadDataBlock(boundingBoxBytes, workspaceId.toString());
            List<BoundingBox3d> boundingBoxes = mapper.readValue(boundingBoxBytes.toByteArray(), new TypeReference<List<BoundingBox3d>>(){});
            return boundingBoxes;
        } catch (Exception e) {
            LOG.error ("Problem fetching fragment bounding boxes from GridFS",e);
            throw new RuntimeException("Problem fetching fragment bounding boxes from GridFS");
        }
    }

    @Override
    public TmWorkspace copyTmWorkspace(String subjectKey, TmWorkspace existingWorkspace, String newName, String assignOwner) {
        // Create a copy of the workspace object with the new name
        TmWorkspace workspaceCopy = createTmWorkspace(subjectKey, TmWorkspace.copy(existingWorkspace).rename(newName));
        try {
            Spliterator<Stream<TmNeuronMetadata>> neuronsSupplier = new Spliterator<Stream<TmNeuronMetadata>>() {
                volatile long offset = 0L;
                int defaultLength = 100000;

                @Override
                public boolean tryAdvance(Consumer<? super Stream<TmNeuronMetadata>> action) {
                    List<TmNeuronMetadata> tmNeurons = tmNeuronMetadataDao.getTmNeuronMetadataByWorkspaceId(existingWorkspace,
                            subjectKey, offset, defaultLength, false);
                    long lastEntryOffset = offset + tmNeurons.size();
                    LOG.info("Retrieved {} neurons ({} - {})", tmNeurons.size(), offset, lastEntryOffset);
                    if (tmNeurons.isEmpty()) {
                        return false;
                    } else {
                        offset = lastEntryOffset;
                        action.accept(tmNeurons.stream());
                        return tmNeurons.size() == defaultLength; // if the number of retrieved neurons is less than requested - it reached the end
                    }
                }

                @Override
                public Spliterator<Stream<TmNeuronMetadata>> trySplit() {
                    return null;
                }

                @Override
                public long estimateSize() {
                    return Long.MAX_VALUE;
                }

                @Override
                public int characteristics() {
                    return ORDERED;
                }
            };

            // Copy the neurons

            // Create the source for neuron IDs
            StreamSupport.stream(neuronsSupplier, true)
                    .flatMap(neuronStream -> neuronStream)
                    .forEach(target -> {
                        TmNeuronMetadata neuronCopy = TmNeuronMetadata.copy(target);
                        neuronCopy.setWorkspaceRef(Reference.createFor(workspaceCopy));
                        if (assignOwner != null) {
                            neuronCopy.setOwnerKey(assignOwner);
                        }
                        try {
                            // Change the parent of the roots to be the neuron id
                            for (TmGeoAnnotation annotation : neuronCopy.getRootAnnotations()) {
                                annotation.setParentId(neuronCopy.getId());
                            }
                            tmNeuronMetadataDao.createTmNeuronInWorkspace(subjectKey, neuronCopy, workspaceCopy);
                        } catch (Exception e) {
                            LOG.error("Error copying neuron from {} to {}", target, neuronCopy);
                            throw new IllegalStateException(e);
                        }
                    });
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

    /**
     * Deep deletion of a TmWorkspace. First deletes mapped neurons and
     * neuron metadata. If those succeed, only then delete the workspace.
     * @param id id of a TmWorkspace
     * @param subjectKey subject key of user
     * @return total number of records deleted
     */
    @Override
    public long deleteByIdAndSubjectKey(Long id, String subjectKey) {
        TmWorkspace workspace = findById(id);
        if (workspace == null) {
            throw new IllegalArgumentException("Workspace with this id not found: "+id);
        }
        return delete(workspace, subjectKey);
    }

    /**
     * Deep deletion of a TmWorkspace. First deletes mapped neurons and
     * neuron metadata. If those succeed, only then delete the workspace.
     * @param workspace the TmWorkspace to delete
     * @param subjectKey subject key of user
     * @return total number of records deleted
     */
    public long delete(TmWorkspace workspace, String subjectKey) {
        int n = 0;
        try {
            n += tmMappedNeuronDao.deleteNeuronsForWorkspace(workspace, subjectKey);
        }
        catch (Exception e) {
            throw new IllegalStateException("Error deleting mapped neurons for workspace "+workspace, e);
        }
        try {
            n += tmNeuronMetadataDao.deleteNeuronsForWorkspace(workspace, subjectKey);
        }
        catch (Exception e) {
            throw new IllegalStateException("Error deleting neuron metadata for workspace "+workspace, e);
        }
        n += super.deleteByIdAndSubjectKey(workspace.getId(), subjectKey);
        return n;
    }
}
