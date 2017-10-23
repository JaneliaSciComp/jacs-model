package org.janelia.it.jacs.model.domain.gui.search.criteria;

import java.util.HashSet;
import java.util.Set;

public class FacetCriteria extends AttributeCriteria {

    private Set<String> values = new HashSet<>();
    
    public Set<String> getValues() {
        return values;
    }

    public void setValues(Set<String> values) {
        if (values==null) throw new IllegalArgumentException("Property cannot be null");
        this.values = values;
    }
}
