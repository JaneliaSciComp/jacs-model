package org.janelia.it.jacs.model.domain.support;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

/**
 * An indexed attribute on a domain object. 
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class DomainObjectAttribute {

    private final String name;
    private final String label;
    private final String searchKey;
    private final String facetKey;
    private final boolean display;
    private final Method getter;
    private final Method setter;
    
    public DomainObjectAttribute(String name, String label, String searchKey, String facetKey, boolean display, Method getter, Method setter) {
        this.name = name;
        this.label = label;
        this.searchKey = searchKey;
        this.facetKey = facetKey;
        this.display = display;
        this.getter = getter;
        this.setter = setter;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }
    
    public String getSearchKey() {
        return StringUtils.isEmpty(searchKey)?null:searchKey;
    }

    public String getFacetKey() {
        return StringUtils.isEmpty(facetKey)?null:facetKey;
    }

    public boolean isDisplay() {
        return display;
    }

    public Method getGetter() {
        return getter;
    }
    
    public Method getSetter() {
        return setter;
    }
    
    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainObjectAttribute that = (DomainObjectAttribute) o;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
