package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.tiledMicroscope.TmAgentMetadata;
import org.janelia.model.domain.tiledMicroscope.TmReviewTask;

import java.util.List;

public interface TmAgentDao extends DomainObjectDao<TmAgentMetadata> {
    TmAgentMetadata getTmAgentMetadata(Long workspaceId, String subjectKey);
    TmAgentMetadata createTmAgentMetadata(String subjectKey, TmAgentMetadata agentData);
    TmAgentMetadata updateTmAgentMetadata(String subjectKey, TmAgentMetadata agentData);
}
