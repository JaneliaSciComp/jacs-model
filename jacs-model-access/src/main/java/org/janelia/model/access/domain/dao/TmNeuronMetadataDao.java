package org.janelia.model.access.domain.dao;

import java.io.InputStream;
import java.util.List;
import java.util.Collection;
import java.util.stream.Stream;

import com.mongodb.client.FindIterable;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.janelia.model.domain.tiledMicroscope.BulkNeuronStyleUpdate;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

/**
 * TmWorkspace data access object
 */
public interface TmNeuronMetadataDao extends DomainObjectDao<TmNeuronMetadata> {
    TmNeuronMetadata createTmNeuronInWorkspace(String subjectKey, TmNeuronMetadata neuronMetadata, TmWorkspace workspace);
    TmNeuronMetadata getTmNeuronMetadata (String subjectKey, TmWorkspace workspace, Long neuronId);
    List<TmNeuronMetadata> getTmNeuronMetadataByWorkspaceId(TmWorkspace workspace, String subjectKey, long offset, int length);
    Iterable<TmNeuronMetadata> streamWorkspaceNeurons(TmWorkspace workspace, String subjectKey, long offset, int length);
    List<TmNeuronMetadata> getTmNeuronMetadataByNeuronIds(TmWorkspace workspace, List<Long> neuronList);
    boolean removeTmNeuron(Long neuronId, boolean isLarge, TmWorkspace workspace, String subjectKey);
    void updateNeuronStyles(BulkNeuronStyleUpdate bulkNeuronStyleUpdate, TmWorkspace workspace, String subjectKey);
    void removeEmptyNeuronsInWorkspace(TmWorkspace workspace, String subjectKey);
    void bulkMigrateNeuronsInWorkspace(TmWorkspace workspace, Collection<TmNeuronMetadata> neurons, String subjectKey);
    void updateNeuronTagsForNeurons(TmWorkspace workspace, List<Long> neuronIds, List<String> tags, boolean tagState, String subjectKey);
    Long getNeuronCountsForWorkspace(TmWorkspace workspace, String subjectKey);
    TmNeuronMetadata saveNeuronMetadata(TmWorkspace workspace, TmNeuronMetadata neuron, String subjectKey);
}
