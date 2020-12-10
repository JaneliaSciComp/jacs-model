package org.janelia.model.access.domain.dao.mongo;

import java.util.List;

import javax.inject.Inject;

import com.mongodb.client.MongoDatabase;

import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.LSMImageDao;
import org.janelia.model.domain.sample.LSMImage;

/**
 * {@link LSMImage} Mongo DAO.
 */
public class LSMImageMongoDao extends ImageMongoDao<LSMImage> implements LSMImageDao {

    @Inject
    LSMImageMongoDao(MongoDatabase mongoDatabase,
                     TimebasedIdentifierGenerator idGenerator,
                     DomainPermissionsMongoHelper permissionsHelper,
                     DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public List<LSMImage> findEntitiesByExactName(String name) {
        return findEntitiesByExactNameAndClass(name, LSMImage.class, LSMImage.class);
    }

    @Override
    public List<LSMImage> findEntitiesWithMatchingName(String name) {
        return findEntitiesWithMatchingNameAndClass(name, LSMImage.class, LSMImage.class);
    }

}
