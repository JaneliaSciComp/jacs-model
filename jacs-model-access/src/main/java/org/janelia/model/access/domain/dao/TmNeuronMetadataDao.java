package org.janelia.model.access.domain.dao;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.janelia.model.domain.tiledMicroscope.BulkNeuronStyleUpdate;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

/**
 * TmWorkspace data access object
 */
public interface TmNeuronMetadataDao extends DomainObjectDao<TmNeuronMetadata> {
    TmNeuronMetadata createTmNeuronInWorkspace(String subjectKey, TmNeuronMetadata neuronMetadata, TmWorkspace workspace);
    List<TmNeuronMetadata> getTmNeuronMetadataByWorkspaceId(String subjectKey, Long workspaceId, long offset, int length);
    List<Pair<TmNeuronMetadata, InputStream>> getTmNeuronsMetadataWithPointStreamsByWorkspaceId(String subjectKey, TmWorkspace workspace, long offset, int length);
    boolean removeTmNeuron(Long neuronId, String subjectKey);
    void updateNeuronStyles(BulkNeuronStyleUpdate bulkNeuronStyleUpdate, String subjectKey);
    void updateNeuronTagsTagsForNeurons(List<Long> neuronIds, List<String> tags, boolean tagState, String subjectKey);
}
