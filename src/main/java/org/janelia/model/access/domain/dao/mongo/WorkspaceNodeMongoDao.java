package org.janelia.model.access.domain.dao.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.janelia.model.access.domain.dao.WorkspaceNodeDao;
import org.janelia.model.cdi.DaoObjectMapper;
import org.janelia.model.domain.workspace.Workspace;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * {@link Workspace} Mongo DAO.
 */
public class WorkspaceNodeMongoDao extends TreeNodeMongoDao<Workspace> implements WorkspaceNodeDao {

    @Inject
    WorkspaceNodeMongoDao(MongoDatabase mongoDatabase,
                          DomainPermissionsMongoHelper permissionsHelper,
                          DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, permissionsHelper, updateHelper);
    }

    @Override
    public List<Workspace> getAllWorkspaceNodesAccessibleBySubjectKey(String subjectKey, long offset, int length) {
        if (StringUtils.isBlank(subjectKey)) {
            return Collections.emptyList();
        }
        return MongoDaoHelper.find(
                MongoDaoHelper.createFilterCriteria(
                        Filters.eq("class", Workspace.class.getName()),
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
                        Filters.eq("class", Workspace.class.getName()),
                        Filters.eq("owner", subjectKey)),
                null,
                0,
                -1,
                mongoCollection,
                Workspace.class);
    }
}
