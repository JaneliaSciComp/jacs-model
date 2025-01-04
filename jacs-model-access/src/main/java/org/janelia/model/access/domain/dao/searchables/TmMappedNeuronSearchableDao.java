package org.janelia.model.access.domain.dao.searchables;

import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.TmMappedNeuronDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.tiledMicroscope.TmMappedNeuron;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

@AsyncIndex
@Dependent
public class TmMappedNeuronSearchableDao extends AbstractDomainSearchableDao<TmMappedNeuron> implements TmMappedNeuronDao {

    private final TmMappedNeuronDao tmMappedNeuronDao;

    @Inject
    TmMappedNeuronSearchableDao(TmMappedNeuronDao tmMappedNeuronDao,
                                  @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(tmMappedNeuronDao, objectIndexer);
        this.tmMappedNeuronDao = tmMappedNeuronDao;
    }

    @Override
    public List<TmMappedNeuron> getNeuronsForWorkspace(TmWorkspace workspace) {
        return tmMappedNeuronDao.getNeuronsForWorkspace(workspace);
    }

    @Override
    public long deleteNeuronsForWorkspace(TmWorkspace workspace, String subjectKey) {
        return tmMappedNeuronDao.deleteNeuronsForWorkspace(workspace, subjectKey);
    }
}
