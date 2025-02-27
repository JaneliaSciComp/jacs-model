package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.tiledMicroscope.BoundingBox3d;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;
import org.janelia.model.domain.tiledMicroscope.TmWorkspaceInfo;

import java.util.List;

/**
 * TmWorkspace data access object
 */
public interface TmWorkspaceDao extends DomainObjectDao<TmWorkspace> {
    List<TmWorkspace> getTmWorkspacesForSample(String subjectKey, Long sampleId);
    List<TmWorkspace> getAllTmWorkspaces(String subjectKey);
    TmWorkspace createTmWorkspace(String subjectKey, TmWorkspace tmWorkspace);
    TmWorkspace copyTmWorkspace(String subjectKey, TmWorkspace existingWorkspace, String newName, String assignOwner);
    TmWorkspace updateTmWorkspace(String subjectKey, TmWorkspace tmWorkspace);
    void saveWorkspaceBoundingBoxes(TmWorkspace workspace, List<BoundingBox3d> boundingBoxes);
    List<BoundingBox3d> getWorkspaceBoundingBoxes(Long workspaceId);
    List<TmWorkspaceInfo> getLargestWorkspaces(String subjectKey, Long limit);
}
