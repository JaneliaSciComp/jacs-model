package org.janelia.model.domain.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Custom extends Text {

    @JsonIgnore
    public String getTypeName() {
        return "Custom";
    }
}
