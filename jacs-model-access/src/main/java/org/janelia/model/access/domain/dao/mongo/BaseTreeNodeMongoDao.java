package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.dao.TreeNodeDao;
import org.janelia.model.domain.workspace.TreeNode;

import javax.inject.Inject;

/**
 * {@link TreeNode} Mongo DAO.
 */
public class BaseTreeNodeMongoDao extends AbstractTreeNodeMongoDao<TreeNode> implements TreeNodeDao {
    @Inject
    BaseTreeNodeMongoDao(MongoDatabase mongoDatabase,
                         DomainPermissionsMongoHelper permissionsHelper,
                         DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, permissionsHelper, updateHelper);
    }
}
