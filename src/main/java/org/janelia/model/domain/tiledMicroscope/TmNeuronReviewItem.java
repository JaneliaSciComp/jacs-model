package org.janelia.model.domain.tiledMicroscope;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;


public class TmNeuronReviewItem implements TmReviewItem {
    private String pathname;
    private String workspaceRef;
    private String neuronRef;
    private List<Long> annotationIds = new ArrayList<>();
    private boolean reviewed;

    @Override
    public boolean isReviewed() {
        return reviewed;
    }

    @Override
    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    @Override
    public void setName(String name) {
        pathname = name;
    }

    @Override
    public String getName() {
        return pathname;
    }

    @Override
    public List getReviewItems() {
        return annotationIds;
    }

    @Override
    public void addReviewItem(Object item) {
        annotationIds.add((Long)item);

    }

    @JsonIgnore
    @Override
    public String getWorkspaceRef() {
        return workspaceRef;
    }

    @JsonIgnore
    public void setWorkspaceRef(String workspaceRef) {
        this.workspaceRef = workspaceRef;
    }

    @JsonIgnore
    public String getNeuronRef() {
        return neuronRef;
    }

    @JsonIgnore
    public void setNeuronRef(String neuronRef) {
        this.neuronRef = neuronRef;
    }

}
