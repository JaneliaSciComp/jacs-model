package org.janelia.model.access.domain.dao.searchables;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.TmNeuronDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.tiledMicroscope.BulkNeuronStyleUpdate;
import org.janelia.model.domain.tiledMicroscope.TmNeuron;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

/**
 * {@link TmNeuron} DAO.
 */
@AsyncIndex
public class TmNeuronSearchableDao extends AbstractDomainSearchablDao<TmNeuron> implements TmNeuronDao {

    private final TmNeuronDao TmNeuronDao;

    @Inject
    TmNeuronSearchableDao(TmNeuronDao TmNeuronDao,
                          @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(TmNeuronDao, objectIndexer);
        this.TmNeuronDao = TmNeuronDao;
    }

    @Override
    public TmNeuron createTmNeuronInWorkspace(String subjectKey, TmNeuron neuronMetadata, TmWorkspace workspace) {
        TmNeuron persistedNeuron = TmNeuronDao.createTmNeuronInWorkspace(subjectKey, neuronMetadata, workspace);
        domainObjectIndexer.indexDocument(persistedNeuron);
        return persistedNeuron;
    }

    @Override
    public TmNeuron getTmNeuron(String subjectKey, TmWorkspace workspace, Long neuronId) {
        return TmNeuronDao.getTmNeuron(subjectKey, workspace, neuronId);
    }

    @Override
    public Iterable<TmNeuron> streamWorkspaceNeurons(TmWorkspace workspace, String subjectKey, long offset, int length) {
        return TmNeuronDao.streamWorkspaceNeurons(workspace, subjectKey, offset, length);
    }

    @Override
    public List<TmNeuron> getTmNeuronByWorkspaceId(TmWorkspace workspace, String subjectKey, long offset, int length) {
        return TmNeuronDao.getTmNeuronByWorkspaceId(workspace, subjectKey, offset, length);
    }

    @Override
    public List<TmNeuron> getTmNeuronByNeuronIds(TmWorkspace workspace, List<Long> neuronIdList) {
        return TmNeuronDao.getTmNeuronByNeuronIds(workspace, neuronIdList);
    }

    @Override
    public boolean removeTmNeuron(Long neuronId, boolean isLarge, TmWorkspace workspace, String subjectKey) {
        boolean removed = TmNeuronDao.removeTmNeuron(neuronId, isLarge, workspace, subjectKey);
        if (removed) {
            domainObjectIndexer.removeDocument(neuronId);
        }
        return removed;
    }

    @Override
    public void updateNeuronStyles(BulkNeuronStyleUpdate bulkNeuronStyleUpdate, TmWorkspace workspace, String subjectKey) {
        TmNeuronDao.updateNeuronStyles(bulkNeuronStyleUpdate, workspace, subjectKey);
    }

    @Override
    public void removeEmptyNeuronsInWorkspace(TmWorkspace workspace, String subjectKey) {
        TmNeuronDao.removeEmptyNeuronsInWorkspace(workspace, subjectKey);
    }

    @Override
    public void bulkMigrateNeuronsInWorkspace(TmWorkspace workspace, Collection<TmNeuron> neurons,
                                              String subjectKey) {
        TmNeuronDao.bulkMigrateNeuronsInWorkspace(workspace, neurons, subjectKey);
    }

    @Override
    public void updateNeuronTagsForNeurons(TmWorkspace workspace, List<Long> neuronIds, List<String> tags, boolean tagState,
                                           String subjectKey) {
        TmNeuronDao.updateNeuronTagsForNeurons(workspace, neuronIds, tags, tagState, subjectKey);
    }

    @Override
    public Long getNeuronCountsForWorkspace(TmWorkspace workspace, String subjectKey) {
        return TmNeuronDao.getNeuronCountsForWorkspace(workspace, subjectKey);
    }

    @Override
    public TmNeuron saveNeuronMetadata(TmWorkspace workspace, TmNeuron neuron, String subjectKey) {
        return TmNeuronDao.saveNeuronMetadata(workspace, neuron, subjectKey);
    }

}
