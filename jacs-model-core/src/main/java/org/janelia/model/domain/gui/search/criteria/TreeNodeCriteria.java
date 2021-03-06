package org.janelia.model.domain.gui.search.criteria;

import org.janelia.model.domain.Reference;

public class TreeNodeCriteria extends Criteria {

    private String treeNodeName;
    private Reference treeNodeReference;

    public String getTreeNodeName() {
        return treeNodeName;
    }

    public void setTreeNodeName(String treeNodeName) {
        this.treeNodeName = treeNodeName;
    }

    public Reference getTreeNodeReference() {
        return treeNodeReference;
    }

    public void setTreeNodeReference(Reference treeNodeReference) {
        this.treeNodeReference = treeNodeReference;
    }
}
