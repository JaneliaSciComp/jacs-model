package org.janelia.model.access.domain.dao;

import org.apache.commons.lang3.tuple.Pair;
import org.janelia.model.domain.tiledMicroscope.BulkNeuronStyleUpdate;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

import java.io.InputStream;
import java.util.List;

/**
 * TmWorkspace data access object
 */
public interface TmNeuronMetadataDao extends DomainObjectDao<TmNeuronMetadata> {
    TmNeuronMetadata createTmNeuronInWorkspace(String subjectKey, TmNeuronMetadata neuronMetadata, TmWorkspace workspace, InputStream neuronPoints);
    List<TmNeuronMetadata> getTmNeuronMetadataByWorkspaceId(String subjectKey, Long workspaceId);
    List<Pair<TmNeuronMetadata, InputStream>> getTmNeuronsMetadataWithPointStreamsByWorkspaceId(String subjectKey, TmWorkspace workspace);
    void removeTmNeuron(Long neuronId, String subjectKey);
    void updateNeuronPoints(TmNeuronMetadata neuron, InputStream neuronPoints);
    void updateNeuronStyles(BulkNeuronStyleUpdate bulkNeuronStyleUpdate, String subjectKey);
    void updateNeuronTagsTagsForNeurons(List<Long> neuronIds, List<String> tags, boolean tagState, String subjectKey);
}
