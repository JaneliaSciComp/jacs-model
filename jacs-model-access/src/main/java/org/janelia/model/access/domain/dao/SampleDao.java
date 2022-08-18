package org.janelia.model.access.domain.dao;

import java.util.Collection;
import java.util.List;

import org.janelia.model.domain.sample.Sample;

/**
 * Interface for accessing subject info.
 */
public interface SampleDao extends DomainObjectDao<Sample> {
    List<Sample> findMatchingSample(Collection<Long> ids,
                                    Collection<String> dataSetIds,
                                    Collection<String> sampleNames,
                                    Collection<String> slideCodes,
                                    long offset,
                                    int length);
}
