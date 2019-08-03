package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.janelia.model.access.domain.dao.NodeDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.TreeNode;

import java.util.Arrays;
import java.util.List;

/**
 * {@link TreeNode} Mongo DAO.
 */
public abstract class AbstractTreeNodeMongoDao<T extends TreeNode> extends AbstractDomainObjectMongoDao<T> implements NodeDao<T> {
    AbstractTreeNodeMongoDao(MongoDatabase mongoDatabase,
                             DomainPermissionsMongoHelper permissionsHelper,
                             DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, permissionsHelper, updateHelper);
    }

    @Override
    public List<T> getNodeDirectAncestors(Reference nodeReference) {
        return MongoDaoHelper.find(
                Filters.all("children", Arrays.asList(nodeReference)),
                null,
                0,
                -1,
                mongoCollection,
                getEntityType());
    }
}
