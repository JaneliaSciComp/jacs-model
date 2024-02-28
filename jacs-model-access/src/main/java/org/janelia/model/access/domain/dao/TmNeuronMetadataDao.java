package org.janelia.model.access.domain.dao;

import org.janelia.model.access.domain.dao.mongo.TmNeuronMetadataMongoDao;
import org.janelia.model.domain.tiledMicroscope.BulkNeuronStyleUpdate;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmOperation;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

import java.util.Date;
import java.util.List;

/**
 * TmWorkspace data access object
 */
public interface TmNeuronMetadataDao extends DomainObjectDao<TmNeuronMetadata> {
    TmNeuronMetadata createTmNeuronInWorkspace(String subjectKey, TmNeuronMetadata neuronMetadata, TmWorkspace workspace);
    TmNeuronMetadata getTmNeuronMetadata (String subjectKey, TmWorkspace workspace, Long neuronId);
    List<TmNeuronMetadata> getTmNeuronMetadataByWorkspaceId(TmWorkspace workspace, String subjectKey, long offset, int length, boolean nofrags);
    Iterable<TmNeuronMetadata> streamWorkspaceNeurons(TmWorkspace workspace, String subjectKey, long offset, int length, boolean nofrags);
    List<TmNeuronMetadata> getTmNeuronMetadataByNeuronIds(TmWorkspace workspace, List<Long> neuronList);
    boolean removeTmNeuron(Long neuronId, boolean isLarge, TmWorkspace workspace, String subjectKey);
    void updateNeuronStyles(BulkNeuronStyleUpdate bulkNeuronStyleUpdate, TmWorkspace workspace, String subjectKey);
    void removeEmptyNeuronsInWorkspace(TmWorkspace workspace, String subjectKey);
    void updateNeuronTagsForNeurons(TmWorkspace workspace, List<Long> neuronIds, List<String> tags, boolean tagState, String subjectKey);
    Long getNeuronCountsForWorkspace(TmWorkspace workspace, String subjectKey);
    TmNeuronMetadata saveNeuronMetadata(TmWorkspace workspace, TmNeuronMetadata neuron, String subjectKey);
    void createOperationLog(Long sampleId, Long workspaceId, Long neuronId, TmOperation.Activity operationType, Date timestamp, Long elapsedTime, String subjectKey);
    List<TmOperation> getOperations(Long workspaceId, Long neuronId, Date startDate, Date endDate);
    long deleteNeuronsForWorkspace(TmWorkspace workspace, String subjectKey);
}
