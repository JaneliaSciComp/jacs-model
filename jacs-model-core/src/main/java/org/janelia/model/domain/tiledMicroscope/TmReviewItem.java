package org.janelia.model.domain.tiledMicroscope;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonSubTypes;

import java.util.List;

/**
 * these are individual review items in a ReviewTask.  For a NeuronReviewTask,
 * these would be root-to-leaf paths.  For a PointListTask, these will be list of points.
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.WRAPPER_OBJECT, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value=TmNeuronReviewItem.class, name="neuronreview"),
        @JsonSubTypes.Type(value=TmPointListReviewItem.class, name="pointlistreview")
})
public interface TmReviewItem {
    boolean isReviewed();
    void setReviewed (boolean reviewed);
    void setName(String name);
    String getName();
    List<Object> getReviewItems();
    void addReviewItem(Object item);
}
