package org.janelia.model.access.domain.dao.mongo;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.IdGenerator;
import org.janelia.model.access.domain.dao.TreeNodeDao;
import org.janelia.model.domain.workspace.TreeNode;

/**
 * {@link TreeNode} Mongo DAO.
 */
@Dependent
public class TreeNodeMongoDao extends AbstractNodeMongoDao<TreeNode> implements TreeNodeDao {

    @Inject
    TreeNodeMongoDao(MongoDatabase mongoDatabase,
                     IdGenerator<Long> idGenerator,
                     DomainPermissionsMongoHelper permissionsHelper,
                     DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }
}
