package org.janelia.model.domain.tiledMicroscope;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.support.MongoMapped;

import java.util.Map;

@MongoMapped(collectionName="tmAgent",label="Tiled Microscope Agent Metadata")
public class TmAgentMetadata extends AbstractDomainObject {
    Map<String, String> agentIdMapping;
    Long workspaceId;

    public Map<String, String> getAgentIdMapping() {
        return agentIdMapping;
    }

    public void setAgentIdMapping(Map<String, String> agentIdMapping) {
        this.agentIdMapping = agentIdMapping;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }
}
