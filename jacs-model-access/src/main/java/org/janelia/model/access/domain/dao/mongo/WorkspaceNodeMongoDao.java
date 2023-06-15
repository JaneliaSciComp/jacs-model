package org.janelia.model.access.domain.dao.mongo;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;

import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.WorkspaceNodeDao;
import org.janelia.model.domain.DomainConstants;
import org.janelia.model.domain.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Workspace} Mongo DAO.
 */
public class WorkspaceNodeMongoDao extends AbstractNodeMongoDao<Workspace> implements WorkspaceNodeDao {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceNodeMongoDao.class);

    @Inject
    WorkspaceNodeMongoDao(MongoDatabase mongoDatabase,
                          TimebasedIdentifierGenerator idGenerator,
                          DomainPermissionsMongoHelper permissionsHelper,
                          DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public Workspace createDefaultWorkspace(String subjectKey) {
        Preconditions.checkArgument(StringUtils.isNotBlank(subjectKey));
        FindOneAndUpdateOptions updateOptions = new FindOneAndUpdateOptions();
        updateOptions.returnDocument(ReturnDocument.AFTER);
        updateOptions.upsert(true);

        Date now = new Date();
        Bson fieldsToInsert = Updates.combine(
                Updates.setOnInsert("_id", createNewId()),
                Updates.setOnInsert("name", DomainConstants.NAME_DEFAULT_WORKSPACE),
                Updates.setOnInsert("ownerKey", subjectKey),
                Updates.setOnInsert("readers", ImmutableSet.of(subjectKey)),
                Updates.setOnInsert("writers", ImmutableSet.of(subjectKey)),
                Updates.setOnInsert("creationDate", now),
                Updates.setOnInsert("updatedDate", now),
                Updates.setOnInsert("class", Workspace.class.getName()),
                Updates.setOnInsert("children", Collections.emptyList())
        );

        Workspace workspace = mongoCollection.findOneAndUpdate(
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createFilterByClass(Workspace.class),
                        Filters.eq("ownerKey", subjectKey),
                        Filters.eq("name", DomainConstants.NAME_DEFAULT_WORKSPACE)
                ),
                fieldsToInsert,
                updateOptions
        );
        if (workspace==null) {
            throw new IllegalStateException("Error creating default workspace for "+subjectKey);
        }
        log.info("Created {} for {}", workspace, subjectKey);
        return workspace;
    }

    @Override
    public List<Workspace> getWorkspaceNodesAccessibleBySubjectGroups(String subjectKey, long offset, int length) {
        if (StringUtils.isBlank(subjectKey)) {
            return Collections.emptyList();
        }
        return MongoDaoHelper.find(
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createFilterByClass(Workspace.class),
                        permissionsHelper.createSameGroupReadPermissionFilterForSubjectKey(subjectKey)),
                null,
                offset,
                length,
                mongoCollection,
                Workspace.class);
    }

    @Override
    public List<Workspace> getWorkspaceNodesOwnedBySubjectKey(String subjectKey) {
        if (StringUtils.isBlank(subjectKey)) {
            return Collections.emptyList();
        }
        return MongoDaoHelper.find(
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createFilterByClass(Workspace.class),
                        Filters.eq("ownerKey", subjectKey)),
                null,
                0,
                -1,
                mongoCollection,
                Workspace.class);
    }

    @Override
    public Workspace getDefaultWorkspace(String subjectKey) {
        Preconditions.checkArgument(StringUtils.isNotBlank(subjectKey));
        return MongoDaoHelper.findFirst(
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createFilterByClass(Workspace.class),
                        Filters.eq("ownerKey", subjectKey),
                        Filters.eq("name", DomainConstants.NAME_DEFAULT_WORKSPACE)
                ),
                mongoCollection,
                Workspace.class);
    }
}
