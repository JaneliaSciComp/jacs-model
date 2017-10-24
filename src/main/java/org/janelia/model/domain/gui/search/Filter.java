package org.janelia.model.domain.gui.search;

import java.util.ArrayList;
import java.util.List;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.gui.search.criteria.Criteria;
import org.janelia.model.domain.interfaces.IsParent;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchType;

/**
 * A saved filter on domain objects, acting against the SOLR server. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="filter",label="Filter")
@SearchType(key="filter",label="Filter")
public class Filter extends AbstractDomainObject implements IsParent, Filtering {

    @SearchAttribute(key="search_class_txt",label="Search Class")
    private String searchClass;
    @SearchAttribute(key="search_string_txt",label="Search String")
    private String searchString;
    private List<Criteria> criteriaList = new ArrayList<>();

    @Override
    public boolean hasCriteria() {
        return !criteriaList.isEmpty();
    }
    
    public void addCriteria(Criteria criteria) {
        if (criteriaList.contains(criteria)) {
            return;
        }
        criteriaList.add(criteria);
    }

    public void removeCriteria(Criteria criteria) {
        criteriaList.remove(criteria);
    }

    @Override
    public String getSearchClass() {
        return searchClass;
    }

    public void setSearchClass(String searchClass) {
        this.searchClass = searchClass;
    }

    @Override
    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    @Override
    public List<Criteria> getCriteriaList() {
        return criteriaList;
    }

    public void setCriteriaList(List<Criteria> criteriaList) {
        if (criteriaList==null) throw new IllegalArgumentException("Property cannot be null");
        this.criteriaList = criteriaList;
    }

}
