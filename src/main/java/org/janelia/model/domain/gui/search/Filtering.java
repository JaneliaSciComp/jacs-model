package org.janelia.model.domain.gui.search;

import java.util.List;

import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.gui.search.criteria.Criteria;
import org.janelia.model.domain.interfaces.IsParent;

/**
 * Something that can be treated like a filter for the purposes of executing a search. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface Filtering extends DomainObject, IsParent {
        
    boolean hasCriteria();

    public String getSearchClass();

    public String getSearchString();

    public List<Criteria> getCriteriaList();
    
}
