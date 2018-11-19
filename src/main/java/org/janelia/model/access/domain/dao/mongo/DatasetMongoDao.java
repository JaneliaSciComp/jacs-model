package org.janelia.model.access.domain.dao.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.dao.DatasetDao;
import org.janelia.model.domain.sample.DataSet;

import javax.inject.Inject;

/**
 * {@link DataSet} Mongo DAO.
 */
public class DatasetMongoDao extends AbstractPermissionAwareDomainMongoDao<DataSet> implements DatasetDao {
    @Inject
    DatasetMongoDao(MongoDatabase mongoDatabase, ObjectMapper objectMapper) {
        super(mongoDatabase, objectMapper);
    }
}
