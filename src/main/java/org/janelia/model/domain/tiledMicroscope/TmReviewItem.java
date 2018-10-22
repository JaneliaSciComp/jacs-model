package org.janelia.model.domain.tiledMicroscope;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.Reference;

import java.util.List;

/**
 * these are individual review items in a ReviewTask.  For a NeuronReviewTask,
 * these would be root-to-leaf paths.  For a PointListTask, these will be list of points.
 *
 */
public interface TmReviewItem {
    @JsonIgnore
    public boolean isReviewed();
    @JsonIgnore
    public void setReviewed (boolean reviewed);
    @JsonIgnore
    public void setName(String name);
    @JsonIgnore
    public String getName();
    @JsonIgnore
    public List<Object> getReviewItems();
    @JsonIgnore
    public String getWorkspaceRef();
    @JsonIgnore
    public void addReviewItem(Object item);
}
