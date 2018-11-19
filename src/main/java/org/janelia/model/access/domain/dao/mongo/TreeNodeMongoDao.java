package org.janelia.model.access.domain.dao.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.janelia.model.access.domain.dao.TreeNodeDao;
import org.janelia.model.access.domain.dao.WorkspaceNodeDao;
import org.janelia.model.domain.workspace.TreeNode;
import org.janelia.model.domain.workspace.Workspace;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * {@link TreeNode} Mongo DAO.
 */
public class TreeNodeMongoDao<T extends TreeNode> extends AbstractPermissionAwareDomainMongoDao<T> implements TreeNodeDao<T> {
    @Inject
    TreeNodeMongoDao(MongoDatabase mongoDatabase, ObjectMapper objectMapper) {
        super(mongoDatabase, objectMapper);
    }
}
