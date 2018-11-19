package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.workspace.TreeNode;
import org.janelia.model.domain.workspace.Workspace;

import java.util.List;

/**
 * TreeNodeDao data access object
 */
public interface TreeNodeDao<T extends TreeNode> extends DomainObjectDao<T> {
}
