package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.NDContainerDao;
import org.janelia.model.domain.files.NDContainer;

import jakarta.inject.Inject;

public class NDContainerMongoDao extends AbstractDomainObjectMongoDao<NDContainer> implements NDContainerDao {

    @Inject
    NDContainerMongoDao(MongoDatabase mongoDatabase,
                        TimebasedIdentifierGenerator idGenerator,
                        DomainPermissionsMongoHelper permissionsHelper,
                        DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }
}
