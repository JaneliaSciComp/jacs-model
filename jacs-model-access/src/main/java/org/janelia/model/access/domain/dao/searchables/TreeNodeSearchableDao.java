package org.janelia.model.access.domain.dao.searchables;

import javax.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.TreeNodeDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.workspace.TreeNode;

/**
 * {@link TreeNode} DAO.
 */
@AsyncIndex
public class TreeNodeSearchableDao extends AbstractNodeSearchableDao<TreeNode> implements TreeNodeDao {

    private final TreeNodeDao treeNodeDao;

    @Inject
    TreeNodeSearchableDao(TreeNodeDao treeNodeDao,
                          @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(treeNodeDao, objectIndexer);
        this.treeNodeDao = treeNodeDao;
    }

}
