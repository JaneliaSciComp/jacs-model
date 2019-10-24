package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.Node;
import org.janelia.model.domain.workspace.TreeNode;

import java.util.Collection;
import java.util.List;

/**
 * NodeDao data access object
 * @param <T> actual node type
 */
public interface NodeDao<T extends Node> extends DomainObjectDao<T> {
    List<T> getNodeDirectAncestors(Reference nodeReference);
    List<T> getNodesByParentNameAndOwnerKey(Long parentNodeId, String name, String ownerKey);
}
