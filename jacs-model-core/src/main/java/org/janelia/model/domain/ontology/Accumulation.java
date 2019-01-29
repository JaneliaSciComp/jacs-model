package org.janelia.model.domain.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;
public class Accumulation extends Text {

    public boolean allowsChildren() {
        return false;
    }

    @JsonIgnore
    public String getTypeName() {
        return "Accumulation";
    }
}
