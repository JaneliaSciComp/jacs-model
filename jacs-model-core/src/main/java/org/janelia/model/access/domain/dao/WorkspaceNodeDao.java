package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.workspace.Workspace;

import java.util.List;

/**
 * Workspace data access object
 */
public interface WorkspaceNodeDao extends TreeNodeDao<Workspace> {
    List<Workspace> getWorkspaceNodesAccessibleBySubjectGroups(String subjectKey, long offset, int length);
    List<Workspace> getWorkspaceNodesOwnedBySubjectKey(String subjectKey);
}
