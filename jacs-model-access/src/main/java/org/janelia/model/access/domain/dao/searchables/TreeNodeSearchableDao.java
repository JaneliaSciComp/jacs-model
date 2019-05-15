package org.janelia.model.access.domain.dao.searchables;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.TreeNodeDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.TreeNode;
import org.janelia.model.domain.workspace.Workspace;

import javax.inject.Inject;
import java.util.List;

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

}
