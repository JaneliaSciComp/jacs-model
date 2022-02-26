package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.SyncedPathDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ReverseReference;
import org.janelia.model.domain.files.SyncedPath;
import org.janelia.model.domain.files.SyncedRoot;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class SyncedPathMongoDao extends AbstractDomainObjectMongoDao<SyncedPath> implements SyncedPathDao {

    @Inject
    SyncedPathMongoDao(MongoDatabase mongoDatabase,
                   TimebasedIdentifierGenerator idGenerator,
                   DomainPermissionsMongoHelper permissionsHelper,
                   DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public SyncedRoot createSyncedRoot(String subjectKey, SyncedRoot syncedRoot) {
        if (syncedRoot.getPaths() == null) {
            ReverseReference ref = new ReverseReference();
            ref.setCount(0L);
            ref.setReferringClassName(SyncedPath.class.getSimpleName());
            ref.setReferenceAttr("rootRef");
            syncedRoot.setPaths(ref);
        }
        return (SyncedRoot)saveBySubjectKey(syncedRoot, subjectKey);
    }

    @Override
    public void removeSyncedRoot(String subjectKey, SyncedRoot syncedRoot) {

        // Delete all children first
        MongoDaoHelper.deleteMatchingRecords(mongoCollection,
                Filters.and(MongoDaoHelper.createAttributeFilter("rootRef", Reference.createFor(syncedRoot)),
                permissionsHelper.createWritePermissionFilterForSubjectKey(subjectKey)));

        // Now we can delete the root itself
        deleteByIdAndSubjectKey(syncedRoot.getId(), subjectKey);
    }

    @Override
    public SyncedPath addSyncedPath(String subjectKey, SyncedRoot syncedRoot, SyncedPath syncedPath) {
        SyncedPath savedSyncedPath = saveBySubjectKey(syncedPath, subjectKey);
        updateCount(subjectKey, syncedRoot);
        return savedSyncedPath;
    }

    @Override
    public void removeSyncedPath(String subjectKey, SyncedRoot syncedRoot, SyncedPath syncedPath) {
        deleteByIdAndSubjectKey(syncedPath.getId(), subjectKey);
        updateCount(subjectKey, syncedRoot);
    }

    @Override
    public List<SyncedRoot> getSyncedRoots(String subjectKey) {
        return streamAllByClass(SyncedRoot.class).collect(Collectors.toList());
    }

    private void updateCount(String subjectKey, SyncedRoot syncedRoot) {
        SyncedRoot root = (SyncedRoot)findById(syncedRoot.getId());
        root.getPaths().setCount(countChildren(subjectKey, syncedRoot));
        save(syncedRoot);
    }

    private long countChildren(String subjectKey, SyncedRoot root) {
        return MongoDaoHelper.count(
                MongoDaoHelper.createFilterCriteria(
                        Filters.eq("rootRef", Reference.createFor(root)),
                        permissionsHelper.createReadPermissionFilterForSubjectKey(subjectKey)),
                mongoCollection);
    }

    @Override
    public List<SyncedPath> getChildren(String subjectKey, SyncedRoot root, long offset, int length) {
        return findEntitiesByForeignKeyReadableBySubjectKey(subjectKey,"rootRef", Reference.createFor(root), offset, length);
    }
}
