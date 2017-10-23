package org.janelia.it.jacs.model.domain.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class EnumText extends OntologyTerm {

    private Long valueEnumId;

    public EnumText() {
    }

    public void init(Long valueEnumId) {
        this.valueEnumId = valueEnumId;
    }

    public boolean allowsChildren() {
        return true;
    }

    @JsonIgnore
    public String getTypeName() {
        return "Enumerated Text";
    }

    public Long getValueEnumId() {
        return valueEnumId;
    }

    public void setValueEnumId(Long valueEnumId) {
        this.valueEnumId = valueEnumId;
    }
}
