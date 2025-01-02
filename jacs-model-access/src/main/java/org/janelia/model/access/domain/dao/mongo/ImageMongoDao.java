package org.janelia.model.access.domain.dao.mongo;

import jakarta.inject.Inject;

import com.mongodb.client.MongoDatabase;

import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.ImageDao;
import org.janelia.model.domain.sample.Image;

/**
 * {@link Image} Mongo DAO.
 */
public class ImageMongoDao<T extends Image> extends AbstractDomainObjectMongoDao<T> implements ImageDao<T> {

    @Inject
    ImageMongoDao(MongoDatabase mongoDatabase,
                  TimebasedIdentifierGenerator idGenerator,
                  DomainPermissionsMongoHelper permissionsHelper,
                  DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

}
