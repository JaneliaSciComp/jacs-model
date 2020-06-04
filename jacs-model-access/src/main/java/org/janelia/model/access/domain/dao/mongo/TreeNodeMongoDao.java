package org.janelia.model.access.domain.dao.mongo;

import javax.inject.Inject;

import com.mongodb.client.MongoDatabase;

import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.TreeNodeDao;
import org.janelia.model.domain.workspace.TreeNode;

/**
 * {@link TreeNode} Mongo DAO.
 */
public class TreeNodeMongoDao extends AbstractNodeMongoDao<TreeNode> implements TreeNodeDao {

    @Inject
    TreeNodeMongoDao(MongoDatabase mongoDatabase,
                     TimebasedIdentifierGenerator idGenerator,
                     DomainPermissionsMongoHelper permissionsHelper,
                     DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }
}
