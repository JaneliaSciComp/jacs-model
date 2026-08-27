package org.janelia.model.access.domain.dao;

import java.util.List;

import org.janelia.model.access.domain.SampleQuery;
import org.janelia.model.domain.sample.Sample;

/**
 * Interface for accessing subject info.
 */
public interface SampleDao extends DomainObjectDao<Sample> {
    List<Sample> findMatchingSamples(SampleQuery sampleQuery);
}
