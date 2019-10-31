package org.janelia.model.access.domain.dao.searchables;

import java.util.List;

import org.janelia.model.access.domain.dao.NodeDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.Node;

/**
 * {@link Node} DAO.
 */
public class AbstractNodeSearchableDao<T extends Node> extends AbstractDomainSearchablDao<T> implements NodeDao<T> {

    private final NodeDao<T> nodeDao;

    AbstractNodeSearchableDao(NodeDao<T> nodeDao,
                              DomainObjectIndexer objectIndexer) {
        super(nodeDao, objectIndexer);
        this.nodeDao = nodeDao;
    }

    @Override
    public List<T> getNodeDirectAncestors(Reference nodeReference) {
        return nodeDao.getNodeDirectAncestors(nodeReference);
    }

    @Override
    public List<T> getNodesByParentNameAndOwnerKey(Long parentNodeId, String name, String ownerKey) {
        return nodeDao.getNodesByParentNameAndOwnerKey(parentNodeId, name, ownerKey);
    }

}