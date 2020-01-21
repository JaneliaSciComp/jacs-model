package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.tiledMicroscope.TmNeuron;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * TmNeuron buffer DAO.
 */
public interface TmNeuronBufferDao extends Dao<TmNeuron, Long> {
    void createNeuronWorkspacePoints(Long neuronId, Long workspaceId, InputStream neuronPoints);
    void deleteNeuronPoints(Long neuronId);
    Map<Long, InputStream> streamNeuronPointsByWorkspaceId(Set<Long> neuronIds, Long workspaceId);
    void updateNeuronWorkspacePoints(Long neuronId, Long workspaceId, InputStream neuronPoints);
}
