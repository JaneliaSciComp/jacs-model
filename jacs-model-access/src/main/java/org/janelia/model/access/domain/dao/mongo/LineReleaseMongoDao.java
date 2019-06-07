package org.janelia.model.access.domain.dao.mongo;

import javax.inject.Inject;

import com.mongodb.client.MongoDatabase;

import org.janelia.model.access.domain.dao.LineReleaseDao;
import org.janelia.model.domain.sample.DataSet;
import org.janelia.model.domain.sample.LineRelease;

/**
 * {@link DataSet} Mongo DAO.
 */
public class LineReleaseMongoDao extends AbstractDomainObjectMongoDao<LineRelease> implements LineReleaseDao {
    @Inject
    LineReleaseMongoDao(MongoDatabase mongoDatabase,
                        DomainPermissionsMongoHelper permissionsHelper,
                        DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, permissionsHelper, updateHelper);
    }
}
