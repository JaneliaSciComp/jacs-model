package org.janelia.model.access.domain.dao.mongo;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.IdGenerator;
import org.janelia.model.access.domain.dao.NDContainerDao;
import org.janelia.model.domain.files.NDContainer;

@Dependent
public class NDContainerMongoDao extends AbstractDomainObjectMongoDao<NDContainer> implements NDContainerDao {

    @Inject
    NDContainerMongoDao(MongoDatabase mongoDatabase,
                        IdGenerator<Long> idGenerator,
                        DomainPermissionsMongoHelper permissionsHelper,
                        DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }
}
