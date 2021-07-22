package org.janelia.model.domain.dto.annotation;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class UpdateAnnotationParams {

    private String subjectKey;
    private Long annotationId;
    private String value;

    public String getSubjectKey() {
        return subjectKey;
    }

    public void setSubjectKey(String subjectKey) {
        this.subjectKey = subjectKey;
    }

    public Long getAnnotationId() {
        return annotationId;
    }

    public void setAnnotationId(Long annotationId) {
        this.annotationId = annotationId;
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
                .append("annotationId", annotationId)
                .append("value", value)
                .toString();
    }
}
