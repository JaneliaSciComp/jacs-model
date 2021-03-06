package org.janelia.model.domain.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;
public class Tag extends OntologyTerm {

    public boolean allowsChildren() {
        return true;
    }

    @JsonIgnore
    public String getTypeName() {
        return "Tag";
    }
}
