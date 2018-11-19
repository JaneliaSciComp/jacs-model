package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.workspace.Workspace;

import java.util.List;

/**
 * TmReviewTask data access object
 */
public interface WorkspaceNodeDao extends DomainObjectDao<Workspace> {
    List<Workspace> getAllWorkspaceNodesByOwnerKey(String subjectKey, long offset, int length);
    Workspace getDefaultWorkspaceNodeByOwnerKey(String subjectKey);
}
