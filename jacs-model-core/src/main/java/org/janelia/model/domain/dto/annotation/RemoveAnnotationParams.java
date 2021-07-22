package org.janelia.model.domain.dto.annotation;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class RemoveAnnotationParams {

    private String subjectKey;
    private Long annotationId;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("subjectKey", subjectKey)
                .append("annotationId", annotationId)
                .toString();
    }
}
