package org.janelia.model.access.domain.dao.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.janelia.model.access.domain.dao.WorkspaceNodeDao;
import org.janelia.model.domain.workspace.Workspace;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * {@link Workspace} Mongo DAO.
 */
public class WorkspaceNodeMongoDao extends TreeNodeMongoDao<Workspace> implements WorkspaceNodeDao {

    @Inject
    WorkspaceNodeMongoDao(MongoDatabase mongoDatabase, ObjectMapper objectMapper) {
        super(mongoDatabase, objectMapper);
    }

    @Override
    public List<Workspace> getAllWorkspaceNodesByOwnerKey(String subjectKey, long offset, int length) {
        if (StringUtils.isBlank(subjectKey))
            return Collections.emptyList();
        return MongoDaoHelper.find(
                MongoDaoHelper.createFilterCriteria(
                        Filters.eq("class", Workspace.class.getName()),
                        createSubjectReadPermissionFilter(subjectKey)),
                null,
                offset,
                length,
                mongoCollection,
                Workspace.class);
    }

    @Override
    public Workspace getDefaultWorkspaceNodeByOwnerKey(String subjectKey) {
        if (StringUtils.isBlank(subjectKey)) return null;
        List<Workspace> subjectWorkspaces = MongoDaoHelper.find(
                MongoDaoHelper.createFilterCriteria(
                        Filters.eq("class", Workspace.class.getName()),
                        Filters.eq("owner", subjectKey)),
                null,
                0,
                2,
                mongoCollection,
                Workspace.class);
        return subjectWorkspaces.stream().findFirst()
                .orElse(null);
    }
}
