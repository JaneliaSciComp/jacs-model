package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.dao.TmReviewTaskDao;
import org.janelia.model.domain.tiledMicroscope.TmReviewTask;

import javax.inject.Inject;
import java.util.List;

/**
 * {@link TmReviewTask} Mongo DAO.
 */
public class TmReviewTaskMongoDao extends AbstractDomainObjectMongoDao<TmReviewTask> implements TmReviewTaskDao {

    private final DomainDAO domainDao;

    @Inject
    TmReviewTaskMongoDao(MongoDatabase mongoDatabase,
                         DomainPermissionsMongoHelper permissionsHelper,
                         DomainUpdateMongoHelper updateHelper,
                         DomainDAO domainDao) {
        super(mongoDatabase, permissionsHelper, updateHelper);
        this.domainDao = domainDao;
    }

    @Override
    public List<TmReviewTask> getReviewTasksForSubject(String subjectKey) {
        return findOwnedEntitiesBySubjectKey(subjectKey, 0, -1);
    }

    @Override
    public TmReviewTask createTmReviewTask(String subjectKey, TmReviewTask tmReviewTask) {
        try {
            return domainDao.save(subjectKey, tmReviewTask);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public TmReviewTask updateTmReviewTask(String subjectKey, TmReviewTask tmReviewTask) {
        try {
            return domainDao.save(subjectKey, tmReviewTask);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
