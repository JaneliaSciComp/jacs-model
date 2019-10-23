package org.janelia.model.access.domain.dao.searchables;

import java.util.List;

import javax.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.TreeNodeDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.TreeNode;

/**
 * {@link TreeNode} DAO.
 */
@AsyncIndex
public class TreeNodeSearchableDao extends AbstractDomainSearchablDao<TreeNode> implements TreeNodeDao {

    private final TreeNodeDao treeNodeDao;

    @Inject
    TreeNodeSearchableDao(TreeNodeDao treeNodeDao,
                          @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(treeNodeDao, objectIndexer);
        this.treeNodeDao = treeNodeDao;
    }

    @Override
    public List<TreeNode> getNodeDirectAncestors(Reference nodeReference) {
        return treeNodeDao.getNodeDirectAncestors(nodeReference);
    }

    @Override
    public List<TreeNode> getNodesByParentNameAndOwnerKey(Long parentNodeId, String name, String ownerKey) {
        return treeNodeDao.getNodesByParentNameAndOwnerKey(parentNodeId, name, ownerKey);
    }
}
