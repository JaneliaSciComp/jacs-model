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
    public TmNeuronMetadata getTmNeuronMetadata(String subjectKey, Long neuronId) {
        return tmNeuronMetadataDao.getTmNeuronMetadata(subjectKey, neuronId);
    }

    @Override
    public List<TmNeuronMetadata> getTmNeuronMetadataByWorkspaceId(String subjectKey, Long workspaceId, long offset, int length) {
        return tmNeuronMetadataDao.getTmNeuronMetadataByWorkspaceId(subjectKey, workspaceId, offset, length);
    }

    @Override
    public List<Pair<TmNeuronMetadata, InputStream>> getTmNeuronsMetadataWithPointStreamsByWorkspaceId(String subjectKey, TmWorkspace workspace, long offset, int length) {
        return tmNeuronMetadataDao.getTmNeuronsMetadataWithPointStreamsByWorkspaceId(subjectKey,workspace,offset,length);
    }

    @Override
    public boolean removeTmNeuron(Long neuronId, boolean isLarge, String subjectKey) {
        boolean removed = tmNeuronMetadataDao.removeTmNeuron(neuronId, isLarge, subjectKey);
        if (removed) {
            domainObjectIndexer.removeDocument(neuronId);
        }
        return removed;
    }

    @Override
    public void updateNeuronStyles(BulkNeuronStyleUpdate bulkNeuronStyleUpdate, String subjectKey) {
        tmNeuronMetadataDao.updateNeuronStyles(bulkNeuronStyleUpdate, subjectKey);
    }

    @Override
    public TmNeuronMetadata saveBySubjectKey(TmNeuronMetadata neuron, String subjectKey) {
        return tmNeuronMetadataDao.saveBySubjectKey(neuron,subjectKey);
    }

    @Override
    public void removeEmptyNeuronsInWorkspace(Long workspaceId, String subjectKey) {
        tmNeuronMetadataDao.removeEmptyNeuronsInWorkspace(workspaceId,subjectKey);
    }

    @Override
    public void bulkReplaceNeuronsInWorkspace(Long workspaceId, Collection<TmNeuronMetadata> neurons, String subjectKey) {
        tmNeuronMetadataDao.bulkReplaceNeuronsInWorkspace(workspaceId, neurons, subjectKey);
    }

    @Override
    public void insertTmNeurons(Collection<TmNeuronMetadata> neurons) {
        tmNeuronMetadataDao.insertTmNeurons(neurons);
    }

    @Override
    public void updateNeuronTagsTagsForNeurons(List<Long> neuronIds, List<String> tags, boolean tagState, String subjectKey) {
        tmNeuronMetadataDao.updateNeuronTagsTagsForNeurons(neuronIds, tags, tagState, subjectKey);
    }

}
