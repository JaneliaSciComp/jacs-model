package org.janelia.model.domain.tiledMicroscope;

import java.util.Map;

public class TmAgentMapping {
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
