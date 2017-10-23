package org.janelia.it.jacs.model.domain.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;
public class Text extends OntologyTerm {

    public boolean allowsChildren() {
        return true;
    }
    
    @JsonIgnore
    public String getTypeName() {
        return "Text";
    }
}
