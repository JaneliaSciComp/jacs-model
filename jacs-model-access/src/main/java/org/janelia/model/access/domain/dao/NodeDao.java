package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.TreeNode;

import java.util.List;

/**
 * NodeDao data access object
 */
public interface NodeDao<T extends TreeNode> extends DomainObjectDao<T> {
    List<T> getNodeDirectAncestors(Reference nodeReference);
}
