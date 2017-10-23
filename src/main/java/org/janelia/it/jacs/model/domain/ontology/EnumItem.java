package org.janelia.it.jacs.model.domain.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class EnumItem extends OntologyTerm {

    public boolean allowsChildren() {
        return false;
    }

    @JsonIgnore
    public String getTypeName() {
        return "Item";
    }
}
