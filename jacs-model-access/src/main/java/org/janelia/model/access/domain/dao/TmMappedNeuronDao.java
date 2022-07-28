package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.tiledMicroscope.TmMappedNeuron;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

import java.util.List;

public interface TmMappedNeuronDao extends DomainObjectDao<TmMappedNeuron> {

    List<TmMappedNeuron> getNeuronsForWorkspace(TmWorkspace workspace);

    long deleteNeuronsForWorkspace(TmWorkspace workspace, String subjectKey);
}
