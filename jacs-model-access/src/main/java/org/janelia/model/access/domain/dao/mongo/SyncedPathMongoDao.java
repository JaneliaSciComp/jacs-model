package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.SyncedPathDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.files.SyncedPath;
import org.janelia.model.domain.files.SyncedRoot;

import javax.inject.Inject;
import java.util.List;

public class SyncedPathMongoDao extends AbstractDomainObjectMongoDao<SyncedPath> implements SyncedPathDao {

    @Inject
    SyncedPathMongoDao(MongoDatabase mongoDatabase,
                   TimebasedIdentifierGenerator idGenerator,
                   DomainPermissionsMongoHelper permissionsHelper,
                   DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public List<SyncedPath> getChildren(String subjectKey, SyncedRoot root, long offset, int length) {
        return findEntitiesByForeignKeyReadableBySubjectKey(subjectKey,"rootRef", Reference.createFor(root), offset, length);
    }
}
