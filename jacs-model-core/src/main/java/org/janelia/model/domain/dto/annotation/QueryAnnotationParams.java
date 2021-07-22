package org.janelia.model.domain.dto.annotation;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.janelia.model.domain.Reference;

import java.util.List;

public class QueryAnnotationParams {

    private String subjectKey;
    private List<Reference> references;

    public String getSubjectKey() {
        return subjectKey;
    }

    public void setSubjectKey(String subjectKey) {
        this.subjectKey = subjectKey;
    }

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("subjectKey", subjectKey)
                .append("references", references)
                .toString();
    }
}
