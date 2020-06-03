package org.janelia.model.access.domain.dao;

import java.util.List;

import org.janelia.model.domain.sample.LineRelease;

/**
 * LineRelease data access object
 */
public interface LineReleaseDao extends NodeDao<LineRelease> {
    List<LineRelease> findReleasesByPublishingSites(List<String> publishingSites);
    List<LineRelease> findReleasesByName(List<String> names);
}
