package org.janelia.model.domain.tiledMicroscope;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;


public class TmNeuronReviewItem implements TmReviewItem {
    private String pathname;
    private Long workspaceId;
    private Long neuronId;
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
    public Long getWorkspaceId() {
        return workspaceId;
    }

    @JsonIgnore
    public void setWorkspaceId(Long workspaceRef) {
        this.workspaceId = workspaceId;
    }

    @JsonIgnore
    public Long getNeuronId() {
        return neuronId;
    }

    @JsonIgnore
    public void setNeuronId(Long neuronId) {
        this.neuronId = neuronId;
    }

}
