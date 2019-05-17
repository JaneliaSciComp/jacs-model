package org.janelia.model.access.domain.dao.searchables;

import org.apache.commons.lang3.tuple.Pair;
import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.TmNeuronMetadataDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.tiledMicroscope.BulkNeuronStyleUpdate;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.List;

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
    public TmNeuronMetadata createTmNeuronInWorkspace(String subjectKey, TmNeuronMetadata neuronMetadata, TmWorkspace workspace, InputStream neuronPoints) {
        TmNeuronMetadata persistedNeuron = tmNeuronMetadataDao.createTmNeuronInWorkspace(subjectKey, neuronMetadata, workspace, neuronPoints);
        domainObjectIndexer.indexDocument(persistedNeuron);
        return persistedNeuron;
    }

    @Override
    public List<TmNeuronMetadata> getTmNeuronMetadataByWorkspaceId(String subjectKey, Long workspaceId) {
        return tmNeuronMetadataDao.getTmNeuronMetadataByWorkspaceId(subjectKey, workspaceId);
    }

    @Override
    public List<Pair<TmNeuronMetadata, InputStream>> getTmNeuronsMetadataWithPointStreamsByWorkspaceId(String subjectKey, TmWorkspace workspace) {
        return tmNeuronMetadataDao.getTmNeuronsMetadataWithPointStreamsByWorkspaceId(subjectKey, workspace);
    }

    @Override
    public boolean removeTmNeuron(Long neuronId, String subjectKey) {
        boolean removed = tmNeuronMetadataDao.removeTmNeuron(neuronId, subjectKey);
        if (removed) {
            domainObjectIndexer.removeDocument(neuronId);
        }
        return removed;
    }

    @Override
    public void updateNeuronPoints(TmNeuronMetadata neuron, InputStream neuronPoints) {
        tmNeuronMetadataDao.updateNeuronPoints(neuron, neuronPoints);
    }

    @Override
    public void updateNeuronStyles(BulkNeuronStyleUpdate bulkNeuronStyleUpdate, String subjectKey) {
        tmNeuronMetadataDao.updateNeuronStyles(bulkNeuronStyleUpdate, subjectKey);
    }

    @Override
    public void updateNeuronTagsTagsForNeurons(List<Long> neuronIds, List<String> tags, boolean tagState, String subjectKey) {
        tmNeuronMetadataDao.updateNeuronTagsTagsForNeurons(neuronIds, tags, tagState, subjectKey);
    }

}