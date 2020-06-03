package org.janelia.model.access.domain.dao.searchables;

import java.util.List;

import javax.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.TmReviewTaskDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.tiledMicroscope.TmReviewTask;

/**
 * {@link TmReviewTask} DAO.
 */
@AsyncIndex
public class TmReviewTaskSearchableDao extends AbstractDomainSearchableDao<TmReviewTask> implements TmReviewTaskDao {

    private final TmReviewTaskDao tmReviewTaskDao;

    @Inject
    TmReviewTaskSearchableDao(TmReviewTaskDao tmReviewTaskDao,
                              @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(tmReviewTaskDao, objectIndexer);
        this.tmReviewTaskDao = tmReviewTaskDao;
    }

    @Override
    public List<TmReviewTask> getReviewTasksForSubject(String subjectKey) {
        return tmReviewTaskDao.getReviewTasksForSubject(subjectKey);
    }

    @Override
    public TmReviewTask createTmReviewTask(String subjectKey, TmReviewTask tmReviewTask) {
        TmReviewTask persistedTmReviewTask = tmReviewTaskDao.createTmReviewTask(subjectKey, tmReviewTask);
        domainObjectIndexer.indexDocument(persistedTmReviewTask);
        return persistedTmReviewTask;
    }

    @Override
    public TmReviewTask updateTmReviewTask(String subjectKey, TmReviewTask tmReviewTask) {
        TmReviewTask updatedTmReviewTask = tmReviewTaskDao.updateTmReviewTask(subjectKey, tmReviewTask);
        domainObjectIndexer.indexDocument(updatedTmReviewTask);
        return updatedTmReviewTask;
    }

}
