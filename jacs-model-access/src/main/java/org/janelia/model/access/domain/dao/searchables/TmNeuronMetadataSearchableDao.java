package org.janelia.model.access.domain.dao.searchables;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.TmNeuronMetadataDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.tiledMicroscope.BulkNeuronStyleUpdate;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmOperation;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * {@link TmNeuronMetadata} DAO.
 */
@AsyncIndex
public class TmNeuronMetadataSearchableDao extends AbstractDomainSearchableDao<TmNeuronMetadata> implements TmNeuronMetadataDao {

    private final TmNeuronMetadataDao tmNeuronMetadataDao;

    @Inject
    TmNeuronMetadataSearchableDao(TmNeuronMetadataDao tmNeuronMetadataDao,
                                  @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(tmNeuronMetadataDao, objectIndexer);
        this.tmNeuronMetadataDao = tmNeuronMetadataDao;
    }

    @Override
    public TmNeuronMetadata createTmNeuronInWorkspace(String subjectKey, TmNeuronMetadata neuronMetadata, TmWorkspace workspace) {
        TmNeuronMetadata persistedNeuron = tmNeuronMetadataDao.createTmNeuronInWorkspace(subjectKey, neuronMetadata, workspace);
        domainObjectIndexer.indexDocument(persistedNeuron);
        return persistedNeuron;
    }

    @Override
    public TmNeuronMetadata getTmNeuronMetadata(String subjectKey, TmWorkspace workspace, Long neuronId) {
        return tmNeuronMetadataDao.getTmNeuronMetadata(subjectKey, workspace, neuronId);
    }

    @Override
    public Iterable<TmNeuronMetadata> streamWorkspaceNeurons(TmWorkspace workspace, String subjectKey, long offset, int length) {
        return tmNeuronMetadataDao.streamWorkspaceNeurons(workspace, subjectKey, offset, length);
    }

    @Override
    public List<TmNeuronMetadata> getTmNeuronMetadataByWorkspaceId(TmWorkspace workspace, String subjectKey, long offset, int length) {
        return tmNeuronMetadataDao.getTmNeuronMetadataByWorkspaceId(workspace, subjectKey, offset, length);
    }

    @Override
    public List<TmNeuronMetadata> getTmNeuronMetadataByNeuronIds(TmWorkspace workspace, List<Long> neuronIdList) {
        return tmNeuronMetadataDao.getTmNeuronMetadataByNeuronIds(workspace, neuronIdList);
    }

    @Override
    public boolean removeTmNeuron(Long neuronId, boolean isLarge, TmWorkspace workspace, String subjectKey) {
        boolean removed = tmNeuronMetadataDao.removeTmNeuron(neuronId, isLarge, workspace, subjectKey);
        if (removed) {
            domainObjectIndexer.removeDocument(neuronId);
        }
        return removed;
    }

    @Override
    public void updateNeuronStyles(BulkNeuronStyleUpdate bulkNeuronStyleUpdate, TmWorkspace workspace, String subjectKey) {
        tmNeuronMetadataDao.updateNeuronStyles(bulkNeuronStyleUpdate, workspace, subjectKey);
    }

    @Override
    public void removeEmptyNeuronsInWorkspace(TmWorkspace workspace, String subjectKey) {
        tmNeuronMetadataDao.removeEmptyNeuronsInWorkspace(workspace, subjectKey);
    }

    @Override
    public void updateNeuronTagsForNeurons(TmWorkspace workspace, List<Long> neuronIds, List<String> tags, boolean tagState,
                                           String subjectKey) {
        tmNeuronMetadataDao.updateNeuronTagsForNeurons(workspace, neuronIds, tags, tagState, subjectKey);
    }

    @Override
    public Long getNeuronCountsForWorkspace(TmWorkspace workspace, String subjectKey) {
        return tmNeuronMetadataDao.getNeuronCountsForWorkspace(workspace, subjectKey);
    }

    @Override
    public TmNeuronMetadata saveNeuronMetadata(TmWorkspace workspace, TmNeuronMetadata neuron, String subjectKey) {
        return tmNeuronMetadataDao.saveNeuronMetadata(workspace, neuron, subjectKey);
    }

    @Override
    public void createOperationLog(Long workspaceId, Long neuronId, String operation, Date timestamp, String subjectKey) {
        tmNeuronMetadataDao.createOperationLog(workspaceId, neuronId, operation, timestamp, subjectKey);
    }

    @Override
    public List<TmOperation> getOperations(Long workspaceId, Long neuronId, Date startDate, Date endDate) {
        return tmNeuronMetadataDao.getOperations(workspaceId, neuronId, startDate, endDate);
    }

    @Override
    public long deleteNeuronsForWorkspace(TmWorkspace workspace, String subjectKey) {
        return tmNeuronMetadataDao.deleteNeuronsForWorkspace(workspace, subjectKey);
    }
}
