package org.janelia.model.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SortCriteria {
    public enum SortDirection {
        ASC,
        DESC
    }

    private String field;
    private SortDirection direction;

    public SortCriteria() {
        this(null, SortDirection.ASC);
    }

    /**
     * Create a sort criteria from a string.
     * If the string starts with + or -, this is taken as the sort direction.
     * @param sortCriteriaString sort field optionally starting with plus or minus
     */
    public SortCriteria(String sortCriteriaString) {
        this(sortCriteriaString.replaceFirst("[+\\-]",""),
                sortCriteriaString.startsWith("-") ? SortDirection.DESC : SortDirection.ASC);
    }

    public SortCriteria(String field, SortDirection direction) {
        this.field = field;
        this.direction = direction;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public SortDirection getDirection() {
        return direction;
    }

    public void setDirection(SortDirection direction) {
        this.direction = direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SortCriteria that = (SortCriteria) o;

        return new EqualsBuilder()
                .append(field, that.field)
                .append(direction, that.direction)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(field)
                .append(direction)
                .toHashCode();
    }

}
