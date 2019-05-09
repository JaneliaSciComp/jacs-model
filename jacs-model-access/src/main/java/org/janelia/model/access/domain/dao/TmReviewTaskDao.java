package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.tiledMicroscope.TmReviewTask;

import java.util.List;

/**
 * TmReviewTask data access object
 */
public interface TmReviewTaskDao extends DomainObjectDao<TmReviewTask> {
    List<TmReviewTask> getReviewTasksForSubject(String subjectKey);
    TmReviewTask createTmReviewTask(String subjectKey, TmReviewTask tmReviewTask);
    TmReviewTask updateTmReviewTask(String subjectKey, TmReviewTask tmReviewTask);
}
