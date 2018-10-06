package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.tiledMicroscope.TmSample;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

import java.util.List;

/**
 * TmWorkspace data access object
 */
public interface TmWorkspaceDao extends DomainObjectDao<TmWorkspace> {
    List<TmWorkspace> getTmWorkspacesForSample(String subjectKey, Long sampleId);
}
