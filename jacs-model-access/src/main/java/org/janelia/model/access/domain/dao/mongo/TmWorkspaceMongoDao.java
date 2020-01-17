package org.janelia.model.access.domain.dao.mongo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.apache.commons.lang3.StringUtils;
import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.IdSource;
import org.janelia.model.access.domain.dao.TmNeuronMetadataDao;
import org.janelia.model.access.domain.dao.TmWorkspaceDao;
import org.janelia.model.domain.DomainConstants;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.tiledMicroscope.TmNeuronAnnotation;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmSample;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;
import org.janelia.model.domain.workspace.TreeNode;
import org.janelia.model.util.SortCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TmWorkspace} Mongo DAO.
 */
public class TmWorkspaceMongoDao extends AbstractDomainObjectMongoDao<TmWorkspace> implements TmWorkspaceDao {
    private static final Logger LOG = LoggerFactory.getLogger(TmWorkspaceMongoDao.class);

    private final DomainDAO domainDao;
    private final TmNeuronMetadataDao tmNeuronMetadataDao;
    private final MongoDatabase mongoDatabase;

    @Inject
    TmWorkspaceMongoDao(MongoDatabase mongoDatabase,
                        DomainPermissionsMongoHelper permissionsHelper,
                        DomainUpdateMongoHelper updateHelper,
                        DomainDAO domainDao,
                        TmNeuronMetadataDao tmNeuronMetadataDao) {
        super(mongoDatabase, permissionsHelper, updateHelper);
        this.mongoDatabase = mongoDatabase;
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
    public TmWorkspace createTmWorkspace(String subjectKey, TmWorkspace tmWorkspace) {
        try {
            // find a suitable collection to store this workspace's neurons
            boolean isViable = false;
            String collectionName = MongoDaoHelper.findOrCreateCappedCollection (this, mongoDatabase,
                    "tmNeuron", 20000000000L, TmNeuronMetadata.class);
            tmWorkspace.setNeuronCollection(collectionName);

            TmWorkspace workspace = domainDao.save(subjectKey, tmWorkspace);
            TreeNode folder = domainDao.getOrCreateDefaultTreeNodeFolder(subjectKey, DomainConstants.NAME_TM_WORKSPACE_FOLDER);
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
            Spliterator<Stream<TmNeuronMetadata>> neuronsSupplier = new Spliterator<Stream<TmNeuronMetadata>>() {
                volatile long offset = 0L;
                int defaultLength = 100000;

                @Override
                public boolean tryAdvance(Consumer<? super Stream<TmNeuronMetadata>> action) {
                    List<TmNeuronMetadata> tmNeurons = tmNeuronMetadataDao.getTmNeuronMetadataByWorkspaceId(existingWorkspace,
                            subjectKey, offset, defaultLength);
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
            IdSource neuronIdSource = new IdSource(1000);

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
                            for (TmNeuronAnnotation annotation : neuronCopy.getRootAnnotations()) {
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

}
