package org.janelia.model.access.domain.dao.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.dao.TmReviewTaskDao;
import org.janelia.model.domain.tiledMicroscope.TmReviewTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

/**
 * {@link TmReviewTask} Mongo DAO.
 */
public class TmReviewTaskMongoDao extends AbstractPermissionAwareDomainMongoDao<TmReviewTask> implements TmReviewTaskDao {
    private static final Logger LOG = LoggerFactory.getLogger(TmReviewTaskMongoDao.class);

    private final DomainDAO domainDao;

    @Inject
    TmReviewTaskMongoDao(MongoDatabase mongoDatabase, ObjectMapper objectMapper, DomainDAO domainDao) {
        super(mongoDatabase, objectMapper);
        this.domainDao = domainDao;
    }

    @Override
    public List<TmReviewTask> getReviewTasksForSubject(String subjectKey) {
        return domainDao.getUserDomainObjects(subjectKey, TmReviewTask.class);
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
