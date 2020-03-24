package org.janelia.model.access.domain.search;

/**
 * This is the correspondent to a SOLR facet.
 */
public class FacetValue {
    private String value;
    private long count;

    public FacetValue() {
    }

    public FacetValue(String value, long count) {
        this.value = value;
        this.count = count;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getValue() {
        return value;
    }

    public long getCount() {
        return count;
    }
}