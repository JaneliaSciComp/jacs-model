package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.Node;

import java.util.List;

/**
 * NodeDao data access object
 * @param <T> actual node type
 */
public interface NodeDao<T extends Node> extends DomainObjectDao<T> {
    List<? extends Node> getNodeDirectAncestors(Reference nodeReference);
    List<T> getNodesByParentNameAndOwnerKey(Long parentNodeId, String name, String ownerKey);
    List<DomainObject> getChildren(String subjectKey, Node node, String sortCriteriaStr, long page, int pageSize);
}
