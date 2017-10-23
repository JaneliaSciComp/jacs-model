package org.janelia.it.jacs.model.domain.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Interval extends OntologyTerm {

    private Long lowerBound;
    private Long upperBound;

    public Interval() {
    }

    public void init(Long lowerBound, Long upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        if (lowerBound.compareTo(upperBound)>=0) {
            throw new IllegalArgumentException("Lower bound must be less than upper bound");
        }
    }

    public boolean allowsChildren() {
        return true;
    }

    @JsonIgnore
    public String getTypeName() {
        return "Interval";
    }

    public Long getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(Long lowerBound) {
        this.lowerBound = lowerBound;
    }

    public Long getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(Long upperBound) {
        this.upperBound = upperBound;
    }
}
