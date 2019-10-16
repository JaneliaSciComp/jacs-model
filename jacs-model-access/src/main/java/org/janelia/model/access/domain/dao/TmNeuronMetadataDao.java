package org.janelia.model.access.domain.dao;

import java.io.InputStream;
import java.util.List;
import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;
import org.janelia.model.domain.tiledMicroscope.BulkNeuronStyleUpdate;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

/**
 * TmWorkspace data access object
 */
public interface TmNeuronMetadataDao extends DomainObjectDao<TmNeuronMetadata> {
    TmNeuronMetadata createTmNeuronInWorkspace(String subjectKey, TmNeuronMetadata neuronMetadata, TmWorkspace workspace);
    TmNeuronMetadata getTmNeuronMetadata (String subjectKey, Long neuronId);
    List<TmNeuronMetadata> getTmNeuronMetadataByWorkspaceId(String subjectKey, Long workspaceId, long offset, int length);
    List<Pair<TmNeuronMetadata, InputStream>> getTmNeuronsMetadataWithPointStreamsByWorkspaceId(String subjectKey, TmWorkspace workspace, long offset, int length);
    boolean removeTmNeuron(Long neuronId, boolean isLarge, String subjectKey);
    void updateNeuronStyles(BulkNeuronStyleUpdate bulkNeuronStyleUpdate, String subjectKey);
    void removeEmptyNeuronsInWorkspace(Long workspaceId, String subjectKey);
    void bulkReplaceNeuronsInWorkspace(Long workspaceId, Collection<TmNeuronMetadata> neurons, String subjectKey);
    void insertTmNeurons(Collection<TmNeuronMetadata> neurons);
    void updateNeuronTagsTagsForNeurons(List<Long> neuronIds, List<String> tags, boolean tagState, String subjectKey);
}
