package org.janelia.model.domain.dto.annotation;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ontology.OntologyTermReference;

public class CreateAnnotationParams {

    private String subjectKey;
    private Reference target;
    private OntologyTermReference ontologyTermReference;
    private String value;

    public String getSubjectKey() {
        return subjectKey;
    }

    public void setSubjectKey(String subjectKey) {
        this.subjectKey = subjectKey;
    }

    public Reference getTarget() {
        return target;
    }

    public void setTarget(Reference target) {
        this.target = target;
    }

    public OntologyTermReference getOntologyTermReference() {
        return ontologyTermReference;
    }

    public void setOntologyTermReference(OntologyTermReference ontologyTermReference) {
        this.ontologyTermReference = ontologyTermReference;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("subjectKey", subjectKey)
                .append("target", target)
                .append("ontologyTermReference", ontologyTermReference)
                .append("value", value)
                .toString();
    }
}
