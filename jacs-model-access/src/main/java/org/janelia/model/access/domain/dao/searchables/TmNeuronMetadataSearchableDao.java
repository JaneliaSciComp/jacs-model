package org.janelia.model.access.domain.dao.searchables;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;
import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.TmNeuronMetadataDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.tiledMicroscope.BulkNeuronStyleUpdate;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

/**
 * {@link TmNeuronMetadata} DAO.
 */
@AsyncIndex
public class TmNeuronMetadataSearchableDao extends AbstractDomainSearchablDao<TmNeuronMetadata> implements TmNeuronMetadataDao {

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
    public List<TmNeuronMetadata> getTmNeuronMetadataByWorkspaceId(TmWorkspace workspace, String subjectKey, long offset, int length) {
        return tmNeuronMetadataDao.getTmNeuronMetadataByWorkspaceId(workspace, subjectKey, offset, length);
    }

    @Override
    public List<TmNeuronMetadata> getTmNeuronMetadataByNeuronIds(TmWorkspace workspace, List<Long> neuronIdList) {
        return tmNeuronMetadataDao.getTmNeuronMetadataByNeuronIds(workspace, neuronIdList);
    }

    @Override
    public List<Pair<TmNeuronMetadata, InputStream>> getTmNeuronsMetadataWithPointStreamsByWorkspaceId(TmWorkspace workspace,
                                                                                                       String subjectKey,
                                                                                                       long offset, int length) {
        return tmNeuronMetadataDao.getTmNeuronsMetadataWithPointStreamsByWorkspaceId(workspace, subjectKey,offset,length);
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
    public void bulkMigrateNeuronsInWorkspace(TmWorkspace workspace, Collection<TmNeuronMetadata> neurons,
                                              String subjectKey) {
        tmNeuronMetadataDao.bulkMigrateNeuronsInWorkspace(workspace, neurons, subjectKey);
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

}
