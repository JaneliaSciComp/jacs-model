package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.tiledMicroscope.TmSample;

/**
 * TmSample data access object
 */
public interface TmSampleDao extends DomainObjectDao<TmSample> {
    TmSample createTmSample(String subjectKey, TmSample tmSample);
    TmSample updateTmSample(String subjectKey, TmSample tmSample);
    boolean removeTmSample(String subjectKey, Long tmSampleId);
}
