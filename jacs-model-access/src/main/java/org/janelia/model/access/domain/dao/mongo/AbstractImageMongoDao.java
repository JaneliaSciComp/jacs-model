package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.IdGenerator;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.ImageDao;
import org.janelia.model.domain.sample.Image;

/**
 * {@link Image} Mongo DAO.
 */
public class AbstractImageMongoDao<T extends Image> extends AbstractDomainObjectMongoDao<T> implements ImageDao<T> {
    AbstractImageMongoDao(MongoDatabase mongoDatabase,
                          IdGenerator<Long> idGenerator,
                          DomainPermissionsMongoHelper permissionsHelper,
                          DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }
}
