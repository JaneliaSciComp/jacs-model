package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.dao.TmSampleDao;
import org.janelia.model.domain.tiledMicroscope.TmSample;

import javax.inject.Inject;

/**
 * TmSample Mongo DAO.
 *
 * @param <T> type of the element
 */
public class TmSampleMongoDao extends AbstractPermissionAwareDomainMongoDao<TmSample> implements TmSampleDao {
    @Inject
    TmSampleMongoDao(MongoDatabase mongoDatabase) {
        super(mongoDatabase);
    }
}
