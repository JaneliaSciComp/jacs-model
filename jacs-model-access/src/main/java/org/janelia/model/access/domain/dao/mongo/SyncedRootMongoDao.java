package org.janelia.model.access.domain.dao.mongo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.DaoUpdateResult;
import org.janelia.model.access.domain.dao.EntityFieldValueHandler;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.access.domain.dao.SyncedRootDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.files.SyncedRoot;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class SyncedRootMongoDao extends AbstractDomainObjectMongoDao<SyncedRoot> implements SyncedRootDao {

    @Inject
    SyncedRootMongoDao(MongoDatabase mongoDatabase,
                       TimebasedIdentifierGenerator idGenerator,
                       DomainPermissionsMongoHelper permissionsHelper,
                       DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public SyncedRoot createSyncedRoot(String subjectKey, SyncedRoot syncedRoot) {
        return saveBySubjectKey(syncedRoot, subjectKey);
    }

    @Override
    public void removeSyncedRoot(String subjectKey, SyncedRoot syncedRoot) {
        deleteByIdAndSubjectKey(syncedRoot.getId(), subjectKey);
    }

    @Override
    public List<SyncedRoot> getSyncedRoots(String subjectKey) {
        return MongoDaoHelper.find(
                permissionsHelper.createSameGroupReadPermissionFilterForSubjectKey(subjectKey),
                null,
                null,
                0,
                -1,
                mongoCollection,
                SyncedRoot.class);
    }

    @Override
    public SyncedRoot updateChildren(String subjectKey, SyncedRoot syncedRoot, List<Reference> newChildren) {
        ImmutableMap.Builder<String, EntityFieldValueHandler<?>> updatesBuilder = ImmutableMap.builder();
        updatesBuilder.put("children", new SetFieldValueHandler<>(newChildren));
        Map<String, EntityFieldValueHandler<?>> updates = updatesBuilder.build();
        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(false);
        DaoUpdateResult daoUpdateResult = MongoDaoHelper.updateMany(
                mongoCollection,
                MongoDaoHelper.createFilterCriteria(
                        ImmutableList.of(
                                MongoDaoHelper.createFilterById(syncedRoot.getId()),
                                permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey))
                ),
                updates,
                updateOptions);
        if (daoUpdateResult.getEntitiesFound()==1) {
            syncedRoot.setChildren(newChildren);
            return syncedRoot;
        }
        return null;
    }
}
