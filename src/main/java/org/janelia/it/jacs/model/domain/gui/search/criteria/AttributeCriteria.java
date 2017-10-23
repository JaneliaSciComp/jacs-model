package org.janelia.it.jacs.model.domain.gui.search.criteria;

public abstract class AttributeCriteria extends Criteria {

    private String attributeName;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

}
